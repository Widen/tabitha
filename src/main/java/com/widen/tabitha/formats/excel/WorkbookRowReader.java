package com.widen.tabitha.formats.excel;

import com.widen.tabitha.*;
import com.widen.tabitha.Row;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * Reads rows from an Excel spreadsheet file using the Apache workbook API.
 * <p>
 * This reader loads all spreadsheet data into memory when first opened, so this reader should be avoided when working
 * with large files or in a memory-constrained environment.
 */
public class WorkbookRowReader implements PagedReader {
    private final Workbook workbook;
    private Sheet sheet;
    private Schema schema;
    private int currentRow;

    public WorkbookRowReader(File file) throws InvalidFormatException, IOException {
        this(WorkbookFactory.create(file));
    }

    public WorkbookRowReader(InputStream inputStream) throws InvalidFormatException, IOException {
        this(WorkbookFactory.create(inputStream));
    }

    public WorkbookRowReader(Workbook workbook) {
        this.workbook = workbook;
        seekPage(0);
    }

    @Override
    public int getPageIndex() {
        return workbook.getSheetIndex(sheet);
    }

    @Override
    public Optional<String> getPageName() {
        return Optional.ofNullable(sheet.getSheetName());
    }

    @Override
    public boolean nextPage() {
        return seekPage(getPageIndex() + 1);
    }

    @Override
    public boolean seekPage(int index) {
        Sheet sheet = workbook.getSheetAt(index);

        return setSheet(sheet);
    }

    @Override
    public boolean seekPage(String name) {
        Sheet sheet = workbook.getSheet(name);

        return setSheet(sheet);
    }

    private boolean setSheet(Sheet sheet) {
        if (sheet != null) {
            this.sheet = sheet;
            currentRow = 1;
            schema = null;

            return true;
        }

        return false;
    }

    /**
     * Get the index of the current row.
     *
     * @return The current row index.
     */
    public int getRowIndex() {
        return currentRow;
    }

    /**
     * Get the row at the given index.
     *
     * @return The row if the index is valid.
     */
    public Optional<Row> getRow(int index) {
        if (schema == null) {
            readSchema();
        }

        org.apache.poi.ss.usermodel.Row row = sheet.getRow(index);

        if (row != null) {
            Variant[] values = new Variant[row.getLastCellNum()];

            for (int i = 0; i < row.getLastCellNum(); ++i) {
                values[i] = getCellValue(row.getCell(i));
            }

            return Optional.of(schema.createRow(values));
        }

        return Optional.empty();
    }

    @Override
    public Optional<Row> read() throws IOException {
        // We've reached the end of the current sheet.
        if (currentRow > sheet.getLastRowNum()) {
            return Optional.empty();
        }

        return getRow(currentRow++);
    }

    private void readSchema() {
        org.apache.poi.ss.usermodel.Row row = sheet.getRow(0);

        if (row != null) {
            Schema.Builder builder = new Schema.Builder();

            for (int i = 0; i < row.getLastCellNum(); ++i) {
                builder.add(getCellValue(row.getCell(i)).toString());
            }

            schema = builder.build();
        }
    }

    private Variant getCellValue(Cell cell) {
        if (cell == null) {
            return Variant.NONE;
        }

        switch (cell.getCellTypeEnum()) {
            case STRING:
                String string = cell.getRichStringCellValue().getString();
                if (StringUtils.isNotBlank(string)) {
                    return new Variant.String(string);
                }
                return Variant.NONE;

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date dateVal = cell.getDateCellValue();
                    return new Variant.String(new SimpleDateFormat().format(dateVal));
                }
                return new Variant.Float(cell.getNumericCellValue());

            case BOOLEAN:
                return Variant.of(cell.getBooleanCellValue());

            case BLANK:
                return Variant.NONE;

            default:
                throw new RuntimeException("Unexpected Cell type at row " + cell.getRowIndex() + ", col " + CellReference.convertNumToColString(cell.getColumnIndex()) + ", [" + cell.getCellTypeEnum().name() + "]");
        }
    }
}
