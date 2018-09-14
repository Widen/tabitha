package com.widen.tabitha.formats.excel;

import com.widen.tabitha.ReaderOptions;
import com.widen.tabitha.Row;
import com.widen.tabitha.RowReader;
import com.widen.tabitha.Variant;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.CellRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.MulBlankRecord;
import org.apache.poi.hssf.record.MulRKRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFactory;
import org.apache.poi.hssf.record.RecordFactoryInputStream;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Streams rows from an Excel binary spreadsheet file.
 */
public class XLSRowReader implements RowReader {
    private final POIFSFileSystem fileSystem;
    private final InputStream documentStream;
    private final RecordFactoryInputStream recordStream;
    private final ReaderOptions options;

    private SSTRecord stringTable;

    // Name of the current sheet.
    private String sheetName;

    // Index of the current row.
    private int rowIndex = -1;

    // Records that have already been read that are next to consume.
    private ArrayDeque<Record> recordBuffer = new ArrayDeque<>();

    /**
     * Open an XLS file from the file system.
     *
     * @param file The file to open.
     * @param options Options to pass to the reader.
     * @return A new row reader.
     */
    public static XLSRowReader open(File file, ReaderOptions options) throws IOException {
        return new XLSRowReader(new POIFSFileSystem(file), options);
    }

    /**
     * Open an XLS file from a stream.
     *
     * @param inputStream The stream to open.
     * @param options Options to pass to the reader.
     * @return A new row reader.
     */
    public static XLSRowReader open(InputStream inputStream, ReaderOptions options) throws IOException {
        return new XLSRowReader(new POIFSFileSystem(inputStream), options);
    }

    private XLSRowReader(POIFSFileSystem poifsFileSystem, ReaderOptions options) throws IOException {
        fileSystem = poifsFileSystem;
        documentStream = fileSystem.createDocumentInputStream("Workbook");
        recordStream = new RecordFactoryInputStream(documentStream, false);
        this.options = options != null ? options : new ReaderOptions();
    }

    @Override
    public Optional<Row> read() throws IOException {
        if (sheetName == null) {
            if (!nextPage()) {
                return Optional.empty();
            }
        }

        Collection<Variant> values = readValues();

        if (values != null) {
            return Optional.of(Row.create(values));
        }

        return Optional.empty();
    }

    @Override
    public void close() throws IOException {
        documentStream.close();
        fileSystem.close();
    }

    private boolean nextPage() {
        rowIndex = -1;

        while (true) {
            Record record = nextRecord();

            if (record == null) {
                break;
            }

            if (record.getSid() == BoundSheetRecord.sid) {
                BoundSheetRecord bsRecord = (BoundSheetRecord) record;

                if (bsRecord.isHidden() && !options.isIncludeHiddenRows()) {
                    continue;
                }

                sheetName = bsRecord.getSheetname();
                return true;
            }
        }

        return false;
    }

    private Collection<Variant> readValues() {
        // We can't read any values if we are at the end of the stream.
        if (peekRecord() == null) {
            return null;
        }

        rowIndex++;
        ArrayList<Variant> cells = new ArrayList<>();

        while (true) {
            Record record = peekRecord();

            // This implicitly signifies the end of the file.
            if (record == null) {
                break;
            }

            // These appear in the middle of the cell records, to
            // specify that the next bunch are empty but styled.
            // Expand this out into multiple blank cells.
            if (record.getSid() == MulBlankRecord.sid) {
                nextRecord();
                recordBuffer.addAll(Arrays.asList(
                    RecordFactory.convertBlankRecords((MulBlankRecord) record)
                ));

                continue;
            }

            // This is multiple consecutive number cells in one record
            // Expand this out into multiple regular number cells
            if (record.getSid() == MulRKRecord.sid) {
                nextRecord();
                recordBuffer.addAll(Arrays.asList(
                    RecordFactory.convertRKRecords((MulRKRecord) record)
                ));

                continue;
            }

            // Indicates an actual cell, which is what we are interested in.
            if (record instanceof CellRecord) {
                CellRecord cellRecord = (CellRecord) record;

                // Make sure the cell actually belongs to the row we are currently reading.
                if (cellRecord.getRow() == rowIndex) {
                    // We're going to accept this record as a cell value, whatever the type.
                    nextRecord();

                    // Fill in any "missing" / blank cells.
                    while (cells.size() < cellRecord.getColumn()) {
                        cells.add(Variant.NONE);
                    }

                    // Parse a numeric cell.
                    if (record.getSid() == NumberRecord.sid) {
                        NumberRecord numberRecord = (NumberRecord) record;
                        cells.add(Variant.of(numberRecord.getValue()));
                    }

                    // Parse a shared string cell.
                    else if (record.getSid() == LabelSSTRecord.sid) {
                        int index = ((LabelSSTRecord) record).getSSTIndex();
                        String value = stringTable.getString(index).getString();
                        cells.add(Variant.of(value));
                    }

                    // Any other type of cell we don't support, so just put a blank.
                    else {
                        cells.add(Variant.NONE);
                    }
                }
                else {
                    // The cell belongs to a later row, so we probably have reached the end of the current row.
                    break;
                }
            }

            // Indicates some other meta record type. We don't care.
            else {
                nextRecord();
            }
        }

        return cells;
    }

    // Consume the next record in the stream.
    private Record nextRecord() {
        Record record = recordBuffer.pollFirst();

        if (record == null) {
            record = recordStream.nextRecord();

            if (record != null) {
                // Handle the SST record here since it might occur anywhere...
                if (record.getSid() == SSTRecord.sid) {
                    stringTable = (SSTRecord) record;
                }
            }
            else {
                sheetName = null;
            }
        }

        return record;
    }

    // Peek ahead by one in the record stream. Peeking is necessary because rows and sheets do not have explicit ends.
    private Record peekRecord() {
        if (recordBuffer.isEmpty()) {
            Record record = nextRecord();

            if (record != null) {
                recordBuffer.addLast(record);
            }
        }

        return recordBuffer.peekFirst();
    }
}
