package com.widen.tabitha.formats;

import com.widen.tabitha.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

/**
 * Writes rows to an Excel spreadsheet file. Supports creating XLS and XLSX files.
 * <p>
 * This writer writes rows into memory and flushes changes all at once when {@link #close()} is called. This writer
 * should be avoided in memory-constrained environments.
 */
public class ExcelRowWriter implements PagedWriter {
    private OutputStream output;
    private Workbook workbook;
    private Sheet sheet;
    private boolean headersWritten;
    private int rowIndex;

    /**
     * Create a new Excel row writer.
     *
     * @param output The output stream to write to.
     */
    public ExcelRowWriter(OutputStream output) {
        this(output, false);
    }

    /**
     * Create a new Excel row writer.
     *
     * @param output       The output stream to write to.
     * @param legacyFormat Whether the legacy XLS format should be used.
     */
    public ExcelRowWriter(OutputStream output, boolean legacyFormat) {
        this.output = output;

        if (legacyFormat) {
            workbook = new HSSFWorkbook();
        } else {
            workbook = new XSSFWorkbook();
        }
    }

    @Override
    public int getPageIndex() {
        return workbook.getSheetIndex(getOrCreateSheet());
    }

    @Override
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
        headersWritten = false;
        rowIndex = 0;
    }

    @Override
    public void write(Row row) throws IOException {
        if (!headersWritten) {
            org.apache.poi.ss.usermodel.Row workbookRow = getOrCreateSheet().createRow(rowIndex++);
            Column[] columns = row.columns();

            for (int column = 0; column < columns.length; ++column) {
                Cell workbookCell = workbookRow.createCell(column);
                workbookCell.setCellType(CellType.STRING);
                workbookCell.setCellValue(columns[column].name);
            }

            headersWritten = true;
        }

        org.apache.poi.ss.usermodel.Row workbookRow = getOrCreateSheet().createRow(rowIndex++);

        int column = 0;
        for (Row.Cell cell : row) {
            Variant value = cell.value;
            Cell workbookCell = workbookRow.createCell(column);

            if (value.isNone()) {
                workbookCell.setCellType(CellType.BLANK);
            } else if (value.getInteger().isPresent()) {
                workbookCell.setCellType(CellType.NUMERIC);
                workbookCell.setCellValue(value.getInteger().get());
            } else if (value.getFloat().isPresent()) {
                workbookCell.setCellType(CellType.NUMERIC);
                workbookCell.setCellValue(value.getFloat().get());
            } else if (value.getBoolean().isPresent()) {
                workbookCell.setCellType(CellType.BOOLEAN);
                workbookCell.setCellValue(value.getBoolean().get());
            } else {
                workbookCell.setCellType(CellType.STRING);
                workbookCell.setCellValue(value.toString());
            }

            ++column;
        }
    }

    @Override
    public void close() throws IOException {
        workbook.write(output);
        workbook.close();
    }

    private Sheet getOrCreateSheet() {
        if (sheet == null) {
            beginPage();
        }

        return sheet;
    }
}
