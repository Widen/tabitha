package com.widen.tabitha.io;

import com.widen.tabitha.ColumnIndex;
import com.widen.tabitha.Row;
import com.widen.tabitha.Value;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * Reads rows from an Excel workbook.
 */
public class ExcelRowReader implements RowReader
{
    private Workbook workbook;
    private ColumnIndex columnIndex;
    private int rowIndex = 0;

    public ExcelRowReader(File file) throws InvalidFormatException, IOException
    {
        this(WorkbookFactory.create(file));
    }

    public ExcelRowReader(InputStream inputStream) throws InvalidFormatException, IOException
    {
        this(WorkbookFactory.create(inputStream));
    }

    public ExcelRowReader(Workbook workbook)
    {
        this.workbook = workbook;
    }

    @Override
    public Optional<Row> read() throws IOException
    {
        if (columnIndex == null)
        {
            readHeader();
        }

        org.apache.poi.ss.usermodel.Row row = nextRow();

        if (row != null)
        {
            Value[] values = new Value[row.getLastCellNum()];
            for (int i = 0; i < row.getLastCellNum(); ++i)
            {
                values[i] = getCellValue(row.getCell(i));
            }

            return Optional.of(new Row(columnIndex, values));
        }

        return Optional.empty();
    }

    private void readHeader()
    {
        org.apache.poi.ss.usermodel.Row row = nextRow();

        if (row != null)
        {
            ColumnIndex.Builder builder = new ColumnIndex.Builder();

            for (int i = 0; i < row.getLastCellNum(); ++i)
            {
                builder.addColumn(getCellValue(row.getCell(i)).asString());
            }

            columnIndex = builder.build();
        }
    }

    private org.apache.poi.ss.usermodel.Row nextRow()
    {
        Sheet sheet = workbook.getSheetAt(0);

        if (rowIndex > sheet.getLastRowNum())
        {
            return null;
        }

        org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowIndex);
        rowIndex += 1;

        return row;
    }

    private Value getCellValue(Cell cell)
    {
        if (cell == null)
        {
            return null;
        }

        switch (cell.getCellTypeEnum())
        {
            case STRING:
                return new Value.String(cell.getRichStringCellValue().getString());

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell))
                {
                    Date dateVal = cell.getDateCellValue();
                    return new Value.String(new SimpleDateFormat().format(dateVal));
                }
                return new Value.Float(cell.getNumericCellValue());

            default:
                throw new RuntimeException("Unexpected Cell type at row=$curRowIx, col=$curCol [${curCell.cellType}]");
        }
    }
}
