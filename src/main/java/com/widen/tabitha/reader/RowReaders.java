package com.widen.tabitha.reader;

import com.widen.tabitha.formats.delimited.DelimitedFormat;
import com.widen.tabitha.formats.delimited.DelimitedRowReader;
import com.widen.tabitha.formats.excel.XLSRowReader;
import com.widen.tabitha.formats.excel.XLSXRowReader;
import org.apache.tika.Tika;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Helper factory methods for creating row readers.
 */
public class RowReaders {
    /**
     * Attempt to detect the format of a file at the given path and open it as a row reader.
     *
     * @param path The file path of the file to open.
     * @return A row reader if the file is in a supported format.
     */
    public static Optional<RowReader> open(String path) throws Exception {
        return open(Paths.get(path), null);
    }

    /**
     * Attempt to detect the format of a file at the given path and open it as a row reader.
     *
     * @param path The file path of the file to open.
     * @return A row reader if the file is in a supported format.
     */
    public static Optional<RowReader> open(Path path) throws Exception {
        return open(path, null);
    }

    /**
     * Attempt to detect the format of a file at the given path and open it as a row reader.
     *
     * @param path The file path of the file to open.
     * @param options Options to pass to the reader.
     * @return A row reader if the file is in a supported format.
     */
    public static Optional<RowReader> open(Path path, ReaderOptions options) throws Exception {
        if (options == null) {
            options = new ReaderOptions();
        }

        String mimeType = tika.detect(path);

        switch (mimeType) {
            case "text/csv":
            case "text/plain":
                return Optional.of(decorate(new DelimitedRowReader(Files.newInputStream(path), DelimitedFormat.CSV), options));

            case "text/tab-separated-values":
                return Optional.of(decorate(new DelimitedRowReader(Files.newInputStream(path), DelimitedFormat.TSV), options));

            case "application/vnd.ms-excel":
                return Optional.of(decorate(XLSRowReader.open(path, options), options));

            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
            case "application/x-tika-ooxml":
                return Optional.of(decorate(XLSXRowReader.open(path, options), options));
        }

        return Optional.empty();
    }

    /**
     * Attempt to detect the format of an input stream and open it as a row reader.
     *
     * @param inputStream The input stream to read.
     * @return A row reader if the stream is in a supported format.
     */
    public static Optional<RowReader> open(InputStream inputStream) throws IOException {
        return open(inputStream, null, null);
    }

    /**
     * Attempt to detect the format of an input stream and open it as a row reader.
     *
     * @param inputStream The input stream to read.
     * @param filename The filename associated with the stream, if known.
     * @return A row reader if the stream is in a supported format.
     */
    public static Optional<RowReader> open(InputStream inputStream, String filename) throws IOException {
        return open(inputStream, filename, null);
    }

    /**
     * Attempt to detect the format of an input stream and open it as a row reader.
     *
     * @param inputStream The input stream to read.
     * @param options Options to pass to the reader.
     * @return A row reader if the stream is in a supported format.
     */
    public static Optional<RowReader> open(InputStream inputStream, ReaderOptions options) throws IOException {
        return open(inputStream, null, options);
    }

    /**
     * Attempt to detect the format of an input stream and open it as a row reader.
     *
     * @param inputStream The input stream to read.
     * @param filename The filename associated with the stream, if known.
     * @param options Options to pass to the reader.
     * @return A row reader if the stream is in a supported format.
     */
    public static Optional<RowReader> open(
        InputStream inputStream,
        String filename,
        ReaderOptions options
    ) throws IOException {
        if (options == null) {
            options = new ReaderOptions();
        }

        // If our input stream supports marks, Tika will rewind the stream back to the start for us after detecting the
        // format, so ensure our input stream supports it.
        inputStream = createRewindableInputStream(inputStream);
        String mimeType = tika.detect(inputStream, filename);

        switch (mimeType) {
            case "text/csv":
            case "text/plain":
                return Optional.of(decorate(new DelimitedRowReader(inputStream, DelimitedFormat.CSV), options));

            case "text/tab-separated-values":
                return Optional.of(decorate(new DelimitedRowReader(inputStream, DelimitedFormat.TSV), options));

            case "application/vnd.ms-excel":
                return Optional.of(decorate(XLSRowReader.open(inputStream, options), options));

            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
            case "application/x-tika-ooxml":
                return Optional.of(decorate(XLSXRowReader.open(inputStream, options), options));
        }

        return Optional.empty();
    }

    private static RowReader decorate(RowReader reader, ReaderOptions options) {
        if (options.isInlineHeaders()) {
            reader = new InlineHeaderReader(reader);
        }
        return reader;
    }

    private static InputStream createRewindableInputStream(InputStream inputStream) {
        return inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
    }

    // Apache Tika instance for detecting MIME types.
    private static final Tika tika = new Tika();
}
