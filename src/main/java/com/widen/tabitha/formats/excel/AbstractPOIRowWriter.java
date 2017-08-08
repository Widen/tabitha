package com.widen.tabitha.formats.excel;

import com.widen.tabitha.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

/**
 * Writes rows to an Excel spreadsheet file. Supports creating XLS and XLSX files.
 * <p>
 * This writer writes rows into memory and flushes changes all at once when {@link #close()} is called. This writer
 * should be avoided in memory-constrained environments.
 */
public abstract class AbstractPOIRowWriter implements PagedWriter {
    private OutputStream output;
    private Workbook workbook;
    private Sheet sheet;
    private boolean headersWritten;
    private int rowIndex;

    protected AbstractPOIRowWriter(Workbook workbook, OutputStream output)
    {
        this.output = output;
        this.workbook = workbook;
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
            row.schema().ifPresent(schema -> {
                org.apache.poi.ss.usermodel.Row workbookRow = getOrCreateSheet().createRow(rowIndex++);

                int index = 0;
                for (String column : schema) {
                    Cell workbookCell = workbookRow.createCell(index++);
                    workbookCell.setCellType(CellType.STRING);
                    workbookCell.setCellValue(column);
                }
            });

            headersWritten = true;
        }

        org.apache.poi.ss.usermodel.Row workbookRow = getOrCreateSheet().createRow(rowIndex++);

        int column = 0;
        for (Variant value : row) {
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
        if (output != null) {
            workbook.write(output);
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
