package com.widen.tabitha.plugins.delimited;

import com.opencsv.CSVReader;
import com.widen.tabitha.Variant;
import com.widen.tabitha.reader.Row;
import com.widen.tabitha.reader.RowReader;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;

/**
 * Reads a delimiter-separated text file into rows of values.
 */
public class DelimitedRowReader implements RowReader {
    private CSVReader reader;
    private int currentIndex = 0;

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
        return Optional
            .ofNullable(reader.readNext())
            .map(cells -> Row.fromStream(0, currentIndex++, Arrays
                .stream(cells)
                .map(cell -> {
                    if (StringUtils.isNotBlank(cell)) {
                        return new Variant.String(cell);
                    }
                    return Variant.NONE;
                })
            ));
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
