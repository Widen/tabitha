package com.widen.tabitha.formats;

import com.widen.tabitha.formats.delimited.DelimitedFormat;
import com.widen.tabitha.formats.delimited.DelimitedRowReader;
import com.widen.tabitha.formats.delimited.DelimitedRowWriter;
import com.widen.tabitha.formats.excel.WorkbookRowWriter;
import com.widen.tabitha.formats.excel.XLSRowReader;
import com.widen.tabitha.formats.excel.XLSXRowReader;
import com.widen.tabitha.reader.InlineHeaderReader;
import com.widen.tabitha.reader.ReaderOptions;
import com.widen.tabitha.reader.RowReader;
import com.widen.tabitha.writer.RowWriter;
import io.reactivex.Maybe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Manages the adapters for the file formats supported by Tabitha.
 * <p>
 * You probably want to use {@link com.widen.tabitha.reader.RowReaders} or {@link com.widen.tabitha.writer.RowWriter}
 * instead.
 */
public class FormatRegistry {
    /**
     * Get a format factory for handling the given MIME type.
     *
     * @param mimeType The format MIME type.
     * @return A format adapter, if one could be found.
     */
    public static Maybe<FormatAdapter> forMimeType(String mimeType) {
        switch (mimeType) {
            case "text/csv":
            case "text/plain":
                return Maybe.just(new FormatAdapter() {
                    @Override
                    public RowReader createReader(InputStream inputStream, ReaderOptions options) {
                        return decorateReader(new DelimitedRowReader(inputStream, DelimitedFormat.CSV), options);
                    }

                    @Override
                    public RowWriter createWriter(OutputStream outputStream) {
                        return new DelimitedRowWriter(outputStream, DelimitedFormat.CSV);
                    }
                });

            case "text/tab-separated-values":
                return Maybe.just(new FormatAdapter() {
                    @Override
                    public RowReader createReader(InputStream inputStream, ReaderOptions options) {
                        return decorateReader(new DelimitedRowReader(inputStream, DelimitedFormat.TSV), options);
                    }

                    @Override
                    public RowWriter createWriter(OutputStream outputStream) {
                        return new DelimitedRowWriter(outputStream, DelimitedFormat.TSV);
                    }
                });

            case "application/vnd.ms-excel":
                return Maybe.just(new FormatAdapter() {
                    @Override
                    public RowReader createReader(Path path, ReaderOptions options) throws IOException {
                        return decorateReader(XLSRowReader.open(path, options), options);
                    }

                    @Override
                    public RowReader createReader(InputStream inputStream, ReaderOptions options) throws IOException {
                        return decorateReader(XLSRowReader.open(inputStream, options), options);
                    }

                    @Override
                    public RowWriter createWriter(OutputStream outputStream) {
                        return WorkbookRowWriter.xls(outputStream);
                    }
                });

            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
            case "application/x-tika-ooxml":
                return Maybe.just(new FormatAdapter() {
                    @Override
                    public RowReader createReader(Path path, ReaderOptions options) throws IOException {
                        return decorateReader(XLSXRowReader.open(path, options), options);
                    }

                    @Override
                    public RowReader createReader(InputStream inputStream, ReaderOptions options) throws IOException {
                        return decorateReader(XLSXRowReader.open(inputStream, options), options);
                    }

                    @Override
                    public RowWriter createWriter(OutputStream outputStream) {
                        return WorkbookRowWriter.xlsx(outputStream);
                    }
                });

            default:
                return Maybe.empty();
        }
    }

    private static RowReader decorateReader(RowReader reader, ReaderOptions options) {
        if (options.isInlineHeaders()) {
            reader = new InlineHeaderReader(reader);
        }
        return reader;
    }
}
