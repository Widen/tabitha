package com.widen.tabitha;

import com.widen.tabitha.formats.delimited.DelimitedFormat;
import com.widen.tabitha.formats.delimited.DelimitedRowReader;
import com.widen.tabitha.formats.excel.XLSRowReader;
import com.widen.tabitha.formats.excel.XLSXRowReader;
import org.apache.tika.Tika;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Helper factory methods for creating row readers.
 */
public class RowReaderFactory {
    /**
     * Attempt to detect the format of a file at the given path and open it as a row reader.
     *
     * @return A row reader if the file is in a supported format.
     */
    public static Optional<RowReader> open(String path) throws Exception {
        return open(new File(path));
    }

    /**
     * Attempt to detect the format of a file and open it as a row reader.
     *
     * @return A row reader if the file is in a supported format.
     */
    public static Optional<RowReader> open(File file) throws Exception {
        String mimeType = tika.detect(file);

        switch (mimeType) {
            case "text/csv":
            case "text/plain":
                return Optional.of(new DelimitedRowReader(new FileInputStream(file), DelimitedFormat.CSV));

            case "text/tab-separated-values":
                return Optional.of(new DelimitedRowReader(new FileInputStream(file), DelimitedFormat.TSV));

            case "application/vnd.ms-excel":
                return Optional.of(XLSRowReader.open(file));

            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
            case "application/x-tika-ooxml":
                return Optional.of(XLSXRowReader.open(file));
        }

        return Optional.empty();
    }

    /**
     * Attempt to detect the format of an input stream and open it as a row reader.
     *
     * @return A row reader if the stream is in a supported format.
     */
    public static Optional<RowReader> open(InputStream inputStream) throws IOException {
        return open(inputStream, null);
    }

    /**
     * Attempt to detect the format of an input stream and open it as a row reader.
     *
     * @return A row reader if the stream is in a supported format.
     */
    public static Optional<RowReader> open(InputStream inputStream, String filename) throws IOException {
        // If our input stream supports marks, Tika will rewind the stream back to the start for us after detecting the
        // format, so ensure our input stream supports it.
        inputStream = createRewindableInputStream(inputStream);
        String mimeType = tika.detect(inputStream, filename);

        switch (mimeType) {
            case "text/csv":
            case "text/plain":
                return Optional.of(new DelimitedRowReader(inputStream, DelimitedFormat.CSV));

            case "text/tab-separated-values":
                return Optional.of(new DelimitedRowReader(inputStream, DelimitedFormat.TSV));

            case "application/vnd.ms-excel":
                return Optional.of(XLSRowReader.open(inputStream));

            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
            case "application/x-tika-ooxml":
                return Optional.of(XLSXRowReader.open(inputStream));
        }

        return Optional.empty();
    }

    private static InputStream createRewindableInputStream(InputStream inputStream) {
        return inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
    }

    // Apache Tika instance for detecting MIME types.
    private static final Tika tika = new Tika();
}
