package com.widen.tabitha.formats.excel;

import com.widen.tabitha.PagedReader;
import com.widen.tabitha.Row;
import com.widen.tabitha.Schema;
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
public class XLSRowReader implements PagedReader {
    public boolean ignoreHidden = true;

    private final POIFSFileSystem fileSystem;
    private final InputStream documentStream;
    private final RecordFactoryInputStream recordStream;

    private SSTRecord stringTable;
    private String pageName;
    private int pageIndex = 0;
    private int rowIndex = 0;
    private int columnIndex = 0;
    private Schema schema;

    // Records that have already been read that are next to consume.
    private ArrayDeque<Record> buffer = new ArrayDeque<>();

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
    public int getPageIndex() {
        return pageIndex;
    }

    @Override
    public Optional<String> getPageName() {
        return Optional.ofNullable(pageName);
    }

    @Override
    public boolean nextPage() throws IOException {
        while (true) {
            Record record = nextRecord();

            if (record == null) {
                break;
            }

            if (record.getSid() == BoundSheetRecord.sid) {
                BoundSheetRecord bsRecord = (BoundSheetRecord) record;

                if (ignoreHidden && bsRecord.isHidden()) {
                    continue;
                }

                pageName = bsRecord.getSheetname();
                break;
            }
        }

        return false;
    }

    @Override
    public Optional<Row> read() throws IOException {
        if (pageName == null) {
            nextPage();
        }

        while (true) {
            Record record = peekRecord();

            if (record == null) {
                return Optional.empty();
            }

            // These mark the start of a new row.
            if (record.getSid() == RowRecord.sid) {
                RowRecord rowRecord = (RowRecord) record;

                List<Variant> cells = parseRow();
                // TODO

                break;
            }
        }

        return null;
    }

    @Override
    public void close() throws IOException {
        documentStream.close();
        fileSystem.close();
    }

    // Consume the next record in the stream.
    private Record nextRecord() {
        if (peekRecord() != null) {
            return buffer.removeFirst();
        }

        return null;
    }

    // Peek ahead by one in the record stream. Peeking is necessary because rows and sheets do not have explicit ends.
    private Record peekRecord() {
        if (buffer.isEmpty()) {
            Record record = recordStream.nextRecord();

            if (record != null) {
                buffer.addLast(record);

                // Handle the SST record here since it might occur anywhere...
                if (record.getSid() == SSTRecord.sid) {
                    stringTable = (SSTRecord) record;
                }
            }
        }

        return buffer.peekFirst();
    }

    private List<Variant> parseRow() {
        ArrayList<Variant> cells = new ArrayList<>();

        while (true) {
            Record record = peekRecord();

            // These implicitly signify the end of a row.
            if (record == null || record.getSid() == BoundSheetRecord.sid || record.getSid() == RowRecord.sid) {
                break;
            }

            nextRecord();

            // Parse cell value record types.
            switch (record.getSid()) {
                case NumberRecord.sid:
                    NumberRecord numrec = (NumberRecord) record;
                    cells.add(Variant.of(numrec.getValue()));
                    System.out.println("Cell found with value " + numrec.getValue()
                        + " at row " + numrec.getRow() + " and column " + numrec.getColumn());
                    break;

                case LabelSSTRecord.sid:
                    int index = ((LabelSSTRecord) record).getSSTIndex();
                    String value = stringTable.getString(index).getString();
                    cells.add(Variant.of(value));
                    break;

                // These appear in the middle of the cell records, to
                //  specify that the next bunch are empty but styled
                // Expand this out into multiple blank cells
                case MulBlankRecord.sid:
                    buffer.addAll(Arrays.asList(
                        RecordFactory.convertBlankRecords((MulBlankRecord) record)
                    ));
                    break;

                // This is multiple consecutive number cells in one record
                // Expand this out into multiple regular number cells
                case MulRKRecord.sid:
                    buffer.addAll(Arrays.asList(
                        RecordFactory.convertRKRecords((MulRKRecord) record)
                    ));
                    break;
            }
        }

        return cells;
    }
}
