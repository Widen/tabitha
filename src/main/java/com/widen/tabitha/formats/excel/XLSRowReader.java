package com.widen.tabitha.formats.excel;

import com.widen.tabitha.Variant;
import com.widen.tabitha.reader.ReaderOptions;
import com.widen.tabitha.reader.Row;
import com.widen.tabitha.reader.RowReader;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Streams rows from an Excel binary spreadsheet file.
 */
public class XLSRowReader implements RowReader {
    private final ReaderOptions options;
    private final POIFSFileSystem fileSystem;
    private final InputStream documentStream;
    private final RecordFactoryInputStream recordStream;

    // Temporary buffer of records that have been read but not yet been parsed.
    private final ArrayDeque<Record> recordBuffer = new ArrayDeque<>();

    private Record currentRecord;
    private SSTRecord stringTable;
    private BoundSheetRecord currentSheet;
    private long currentSheetIndex = -1;
    private long currentRowIndex = 0;

    /**
     * Open an XLS file from the file system.
     *
     * @param path The path of the file to open.
     * @param options Options to pass to the reader.
     * @return A new row reader.
     */
    public static XLSRowReader open(Path path, ReaderOptions options) throws IOException {
        return new XLSRowReader(new POIFSFileSystem(path.toFile()), options);
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
    public Optional<Row> read() {
        ArrayList<Variant> currentCells = new ArrayList<>();
        boolean rowFound = false;

        while (advance()) {
            // Store the string table for later.
            if (currentRecord.getSid() == SSTRecord.sid) {
                stringTable = (SSTRecord) currentRecord;
            }

            // This indicates the start of a new sheet.
            else if (currentRecord.getSid() == BoundSheetRecord.sid) {
                currentSheet = (BoundSheetRecord) currentRecord;
                currentSheetIndex++;
                currentRowIndex = 0;

                // If we already populated some cells, then end of sheet implies end of row.
                if (rowFound) {
                    break;
                }
            }

            // These appear in the middle of the cell records, to
            // specify that the next bunch are empty but styled.
            // Expand this out into multiple blank cells.
            else if (currentRecord.getSid() == MulBlankRecord.sid) {
                pushBack(RecordFactory.convertBlankRecords((MulBlankRecord) currentRecord));
            }

            // This is multiple consecutive number cells in one record
            // Expand this out into multiple regular number cells.
            else if (currentRecord.getSid() == MulRKRecord.sid) {
                pushBack(RecordFactory.convertRKRecords((MulRKRecord) currentRecord));
            }

            // Indicates an actual cell, which is what we are interested in.
            else if (currentRecord instanceof CellRecord) {
                CellRecord cellRecord = (CellRecord) currentRecord;

                // Ignore rows in hidden sheets.
                if (!currentSheet.isHidden() || options.isIncludeHiddenRows()) {
                    rowFound = true;

                    // Make sure the cell actually belongs to the row we are currently reading.
                    if (cellRecord.getRow() == currentRowIndex) {
                        // Fill in any "missing" / blank cells.
                        while (currentCells.size() < cellRecord.getColumn()) {
                            currentCells.add(Variant.NONE);
                        }

                        // Parse a numeric cell.
                        if (cellRecord.getSid() == NumberRecord.sid) {
                            NumberRecord numberRecord = (NumberRecord) cellRecord;
                            currentCells.add(Variant.of(numberRecord.getValue()));
                        }

                        // Parse a shared string cell.
                        else if (cellRecord.getSid() == LabelSSTRecord.sid) {
                            int index = ((LabelSSTRecord) cellRecord).getSSTIndex();
                            String value = stringTable.getString(index).getString();
                            currentCells.add(Variant.of(value));
                        }

                        // Any other type of cell we don't support, so just put a blank.
                        else {
                            currentCells.add(Variant.NONE);
                        }
                    }

                    // Cell belongs to a future row, so we need to return empty rows until we reach the row index this
                    // cell belongs to.
                    else if (cellRecord.getRow() > currentRowIndex) {
                        pushBack(cellRecord);
                        break;
                    }
                }
            }
        }

        if (rowFound) {
            return Optional.of(new Row(null, currentSheetIndex, currentRowIndex++, currentCells));
        }

        return Optional.empty();
    }

    @Override
    public void close() throws IOException {
        documentStream.close();
        fileSystem.close();
    }

    // Push the given records onto the stack to be read again in order.
    private void pushBack(Record... records) {
        for (int i = records.length - 1; i >= 0; --i) {
            recordBuffer.push(records[i]);
        }
    }

    // Consume the next record in the stream.
    private boolean advance() {
        currentRecord = recordBuffer.poll();

        if (currentRecord == null) {
            currentRecord = recordStream.nextRecord();
        }

        return currentRecord != null;
    }
}
