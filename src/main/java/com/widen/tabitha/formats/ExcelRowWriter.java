package com.widen.tabitha.formats;

import com.widen.tabitha.Column;
import com.widen.tabitha.Row;
import com.widen.tabitha.RowWriter;
import com.widen.tabitha.Value;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes rows to an Excel spreadsheet file. Supports creating XLS and XLSX files.
 *
 * This writer writes rows into memory and flushes changes all at once when {@link #close()} is called. This writer
 * should be avoided in memory-constrained environments.
 */
public class ExcelRowWriter implements RowWriter
{
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
    public ExcelRowWriter(OutputStream output)
    {
        this(output, false);
    }

    /**
     * Create a new Excel row writer.
     *
     * @param output The output stream to write to.
     * @param legacyFormat Whether the legacy XLS format should be used.
     */
    public ExcelRowWriter(OutputStream output, boolean legacyFormat)
    {
        this(output, legacyFormat, null);
    }

    /**
     * Create a new Excel row writer.
     *
     * @param output The output stream to write to.
     * @param sheetName The name of the sheet to write to.
     */
    public ExcelRowWriter(OutputStream output, String sheetName)
    {
        this(output, false, sheetName);
    }

    /**
     * Create a new Excel row writer.
     *
     * @param output The output stream to write to.
     * @param legacyFormat Whether the legacy XLS format should be used.
     * @param sheetName The name of the sheet to write to.
     */
    public ExcelRowWriter(OutputStream output, boolean legacyFormat, String sheetName)
    {
        this.output = output;

        if (legacyFormat)
        {
            workbook = new HSSFWorkbook();
        }
        else
        {
            workbook = new XSSFWorkbook();
        }

        // Create the first sheet.
        if (sheetName == null)
        {
            sheetName = "Sheet 1";
        }
        createSheet(sheetName);
    }

    /**
     * Create a new Excel sheet with the given name. Subsequent writes will go to this new sheet.
     *
     * @param name The name of the sheet.
     */
    public void createSheet(String name)
    {
        sheet = workbook.createSheet(name);
        headersWritten = false;
        rowIndex = 0;
    }

    @Override
    public void write(Row row) throws IOException
    {
        if (!headersWritten)
        {
            org.apache.poi.ss.usermodel.Row workbookRow = sheet.createRow(rowIndex++);
            Column[] columns = row.columns();

            for (int column = 0; column < columns.length; ++column)
            {
                Cell workbookCell = workbookRow.createCell(column);
                workbookCell.setCellType(CellType.STRING);
                workbookCell.setCellValue(columns[column].name);
            }

            headersWritten = true;
        }

        org.apache.poi.ss.usermodel.Row workbookRow = sheet.createRow(rowIndex++);

        int column = 0;
        for (Row.Cell cell : row)
        {
            Value value = cell.value;
            Cell workbookCell = workbookRow.createCell(column);

            if (value.integerValue().isPresent())
            {
                workbookCell.setCellType(CellType.NUMERIC);
                workbookCell.setCellValue(value.integerValue().get());
            }
            else if (value.floatValue().isPresent())
            {
                workbookCell.setCellType(CellType.NUMERIC);
                workbookCell.setCellValue(value.floatValue().get());
            }
            else
            {
                workbookCell.setCellType(CellType.STRING);
                workbookCell.setCellValue(value.asString());
            }

            ++column;
        }
    }

    @Override
    public void close() throws IOException
    {
        workbook.write(output);
        workbook.close();
    }
}
