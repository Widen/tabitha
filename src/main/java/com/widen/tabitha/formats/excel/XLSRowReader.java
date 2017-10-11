package com.widen.tabitha.formats.excel;

import com.widen.tabitha.Row;
import com.widen.tabitha.RowReader;
import com.widen.tabitha.Variant;
import org.apache.poi.hssf.record.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Streams rows from an Excel binary spreadsheet file.
 */
public class XLSRowReader implements RowReader {
    public boolean ignoreHidden = true;

    private final POIFSFileSystem fileSystem;
    private final InputStream documentStream;
    private final RecordFactoryInputStream recordStream;

    private SSTRecord stringTable;

    // Name of the current sheet.
    private String sheetName;

    // Index of the current sheet.
    private int sheetIndex = -1;

    // Index of the current row.
    private int rowIndex = -1;

    // Records that have already been read that are next to consume.
    private ArrayDeque<Record> recordBuffer = new ArrayDeque<>();

    /**
     * Open an XLS file from the file system.
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static XLSRowReader open(File file) throws IOException {
        return new XLSRowReader(new POIFSFileSystem(file));
    }

    /**
     * Open an XLS file from a stream.
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static XLSRowReader open(InputStream inputStream) throws IOException {
        return new XLSRowReader(new POIFSFileSystem(inputStream));
    }

    private XLSRowReader(POIFSFileSystem poifsFileSystem) throws IOException {
        fileSystem = poifsFileSystem;
        documentStream = fileSystem.createDocumentInputStream("Workbook");
        recordStream = new RecordFactoryInputStream(documentStream, false);
    }

    @Override
    public Optional<String> getPageName() {
        return Optional.ofNullable(sheetName);
    }

    @Override
    public boolean nextPage() throws IOException {
        rowIndex = -1;

        while (true) {
            Record record = nextRecord();

            if (record == null) {
                break;
            }

            if (record.getSid() == BoundSheetRecord.sid) {
                BoundSheetRecord bsRecord = (BoundSheetRecord) record;
                sheetIndex++;

                if (ignoreHidden && bsRecord.isHidden()) {
                    continue;
                }

                sheetName = bsRecord.getSheetname();
                return true;
            }
        }

        return false;
    }

    @Override
    public Optional<Row> read() throws IOException {
        if (sheetName == null) {
            nextPage();
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

    private Collection<Variant> readValues() throws IOException {
        rowIndex++;
        ArrayList<Variant> cells = new ArrayList<>();

        while (true) {
            Record record = peekRecord();

            // These implicitly signify the end of the current sheet.
            if (record == null || record.getSid() == BoundSheetRecord.sid) {
                return null;
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
                } else {
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

            // Handle the SST record here since it might occur anywhere...
            if (record != null && record.getSid() == SSTRecord.sid) {
                stringTable = (SSTRecord) record;
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
