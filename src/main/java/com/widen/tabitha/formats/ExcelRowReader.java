package com.widen.tabitha.formats;

import com.widen.tabitha.Row;
import com.widen.tabitha.RowReader;
import com.widen.tabitha.Schema;
import com.widen.tabitha.Variant;
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
 * Reads rows from an Excel workbook.
 */
public class ExcelRowReader implements RowReader
{
    private Workbook workbook;
    private Schema schema;
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
        if (schema == null)
        {
            readHeader();
        }

        org.apache.poi.ss.usermodel.Row row = nextRow();

        if (row != null)
        {
            Variant[] values = new Variant[row.getLastCellNum()];
            for (int i = 0; i < row.getLastCellNum(); ++i)
            {
                values[i] = getCellValue(row.getCell(i));
            }

            return Optional.of(schema.createRow(values));
        }

        return Optional.empty();
    }

    private void readHeader()
    {
        org.apache.poi.ss.usermodel.Row row = nextRow();

        if (row != null)
        {
            Schema.Builder builder = new Schema.Builder();

            for (int i = 0; i < row.getLastCellNum(); ++i)
            {
                builder.add(getCellValue(row.getCell(i)).toString());
            }

            schema = builder.build();
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

    private Variant getCellValue(Cell cell)
    {
        if (cell == null)
        {
            return Variant.NONE;
        }

        switch (cell.getCellTypeEnum())
        {
            case STRING:
                String string = cell.getRichStringCellValue().getString();
                if (StringUtils.isNotBlank(string))
                {
                    return new Variant.String(string);
                }
                return Variant.NONE;

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell))
                {
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
