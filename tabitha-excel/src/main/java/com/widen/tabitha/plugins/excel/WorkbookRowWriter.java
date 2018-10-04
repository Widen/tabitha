package com.widen.tabitha.plugins.excel;

import com.widen.tabitha.Variant;
import com.widen.tabitha.writer.PagedWriter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Writes rows to an Excel spreadsheet file using the Apache workbook API. Supports creating XLS and XLSX files.
 * <p>
 * This writer writes rows into memory and flushes changes all at once when {@link #close()} is called. For XLSX, this
 * problem is mitigated by using temporary files that are flushed to. There is no such workaround for XLS, so this
 * writer should be avoided when writing to XLS in memory-constrained environments.
 */
public class WorkbookRowWriter implements PagedWriter {
    private static final int STREAMING_WINDOW_SIZE = 1;

    private OutputStream output;
    private Workbook workbook;
    private Sheet sheet;
    private int rowIndex;

    /**
     * Create a new Excel XLSX row writer.
     *
     * @param output The output stream to write to.
     * @return The new row writer.
     */
    public static WorkbookRowWriter xlsx(OutputStream output) {
        return new WorkbookRowWriter(new SXSSFWorkbook(STREAMING_WINDOW_SIZE), output);
    }

    /**
     * Create a new Excel XLSX row writer.
     *
     * @param path The path of the file to write to.
     * @return The new row writer.
     */
    public static WorkbookRowWriter xlsx(Path path) throws IOException {
        return xlsx(Files.newOutputStream(path));
    }

    /**
     * Create a new Excel XLS row writer.
     *
     * @param output The output stream to write to.
     * @return The new row writer.
     */
    public static WorkbookRowWriter xls(OutputStream output) {
        return new WorkbookRowWriter(new HSSFWorkbook(), output);
    }

    /**
     * Create a new Excel XLS row writer.
     *
     * @param path The path of the file to write to.
     * @return The new row writer.
     */
    public static WorkbookRowWriter xls(Path path) throws IOException {
        return xls(Files.newOutputStream(path));
    }

    protected WorkbookRowWriter(Workbook workbook, OutputStream output) {
        this.output = output;
        this.workbook = workbook;
    }

    public int getPageIndex() {
        return workbook.getSheetIndex(getOrCreateSheet());
    }

    public Optional<String> getPageName() {
        return Optional.ofNullable(getOrCreateSheet().getSheetName());
    }

    /**
     * Create a new Excel sheet.
     * <p>
     * The new sheet will be named "Sheet {num}", where {num} is the sheet's offset from the beginning.
     */
    @Override
    public void beginPage() {
        beginPage("Sheet " + workbook.getNumberOfSheets());
    }

    /**
     * Create a new Excel sheet with the given name. Subsequent writes will go to this new sheet.
     *
     * @param name The name of the sheet.
     */
    @Override
    public void beginPage(String name) {
        sheet = workbook.createSheet(name);
        rowIndex = 0;
    }

    @Override
    public void write(List<Variant> cells) throws IOException {
        org.apache.poi.ss.usermodel.Row workbookRow = getOrCreateSheet().createRow(rowIndex++);

        int column = 0;
        for (Variant value : cells) {
            Cell workbookCell = workbookRow.createCell(column);

            if (value.isNone()) {
                workbookCell.setCellType(CellType.BLANK);
            }
            else if (value.getInteger().isPresent()) {
                workbookCell.setCellType(CellType.NUMERIC);
                workbookCell.setCellValue(value.getInteger().get());
            }
            else if (value.getFloat().isPresent()) {
                workbookCell.setCellType(CellType.NUMERIC);
                workbookCell.setCellValue(value.getFloat().get());
            }
            else if (value.getBoolean().isPresent()) {
                workbookCell.setCellType(CellType.BOOLEAN);
                workbookCell.setCellValue(value.getBoolean().get());
            }
            else {
                workbookCell.setCellType(CellType.STRING);
                workbookCell.setCellValue(value.toString());
            }

            ++column;
        }
    }

    @Override
    public void close() throws IOException {
        if (output != null) {
            workbook.write(output);
        }

        // Clean up temporary files.
        if (workbook instanceof SXSSFWorkbook) {
            ((SXSSFWorkbook) workbook).dispose();
        }

        workbook.close();
    }

    private Sheet getOrCreateSheet() {
        if (sheet == null) {
            beginPage();
        }

        return sheet;
    }
}
