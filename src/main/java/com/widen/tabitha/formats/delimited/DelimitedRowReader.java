package com.widen.tabitha.formats.delimited;

import com.opencsv.CSVReader;
import com.widen.tabitha.Row;
import com.widen.tabitha.RowReader;
import com.widen.tabitha.Utils;
import com.widen.tabitha.Variant;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * Reads a delimiter-separated text file into rows of values.
 */
public class DelimitedRowReader implements RowReader {
    private CSVReader reader;

    public DelimitedRowReader(InputStream inputStream, DelimitedFormat format) {
        this.reader = new CSVReader(
            new InputStreamReader(inputStream, format.charset),
            format.delimiter,
            format.quoteCharacter,
            format.escapeCharacter,
            0,
            format.strictQuotes
        );
    }

    @Override
    public Optional<Row> read() throws IOException {
        String[] columns = reader.readNext();
        if (columns == null) {
            return Optional.empty();
        }

        Variant[] values = Utils.mapArray(columns, Variant.class, this::asVariant);

        return Optional.of(Row.create(values));
    }

    private Variant asVariant(String value) {
        if (StringUtils.isNotBlank(value)) {
            return new Variant.String(value);
        }

        return Variant.NONE;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
