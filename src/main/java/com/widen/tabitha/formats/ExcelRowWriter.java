package com.widen.tabitha.formats;

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
 * Writes rows to an Excel spreadsheet file. Supports creating XLS and XLSX.
 */
public class ExcelRowWriter implements RowWriter
{
    private OutputStream output;
    private Workbook workbook;
    private Sheet sheet;
    private int rowIndex = 0;

    public ExcelRowWriter(OutputStream output)
    {
        this(output, false);
    }

    public ExcelRowWriter(OutputStream output, boolean legacyFormat)
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

        sheet = workbook.createSheet("Sheet 1");
    }

    @Override
    public void write(Row row) throws IOException
    {
        org.apache.poi.ss.usermodel.Row workbookRow = sheet.createRow(rowIndex++);

        int column = 0;
        for (Value value : row)
        {
            Cell cell = workbookRow.createCell(column);

            if (value.integerValue().isPresent())
            {
                cell.setCellType(CellType.NUMERIC);
                cell.setCellValue(value.integerValue().get());
            }
            else if (value.floatValue().isPresent())
            {
                cell.setCellType(CellType.NUMERIC);
                cell.setCellValue(value.floatValue().get());
            }
            else
            {
                cell.setCellType(CellType.STRING);
                cell.setCellValue(value.asString());
            }

            ++column;
        }
    }

    @Override
    public void close() throws IOException
    {
        workbook.write(output);
    }
}
