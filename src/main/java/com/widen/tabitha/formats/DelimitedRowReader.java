package com.widen.tabitha.formats;

import com.opencsv.CSVReader;
import com.widen.tabitha.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Reads a delimiter-separated text file into rows of values.
 */
public class DelimitedRowReader implements RowReader {
    private CSVReader reader;
    private Schema schema;

    public DelimitedRowReader(InputStream inputStream, DelimitedTextFormat format) {
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
        if (schema == null) {
            readHeaders();
        }

        String[] columns = reader.readNext();
        if (columns == null) {
            return Optional.empty();
        }

        Variant[] values = Utils.mapArray(columns, Variant.class, this::asVariant);

        return Optional.of(schema.createRow(values));
    }

    private void readHeaders() throws IOException {
        String[] columns = reader.readNext();
        Schema.Builder builder = new Schema.Builder();

        for (String column : columns) {
            builder.add(column);
        }

        schema = builder.build();
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
