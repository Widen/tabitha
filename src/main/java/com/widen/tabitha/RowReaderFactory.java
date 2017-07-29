package com.widen.tabitha;

import com.widen.tabitha.formats.DelimitedRowReader;
import com.widen.tabitha.formats.DelimitedTextFormat;
import com.widen.tabitha.formats.ExcelRowReader;
import com.widen.tabitha.formats.ooxml.OOXMLSpreadsheetRowReader;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.tika.Tika;

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
    public static Optional<RowReader> open(String path) throws IOException {
        return open(new File(path));
    }

    /**
     * Attempt to detect the format of a file and open it as a row reader.
     *
     * @return A row reader if the file is in a supported format.
     */
    public static Optional<RowReader> open(File file) throws IOException {
        String mimeType = tika.detect(file);

        switch (mimeType) {
            case "text/csv":
            case "text/plain":
                return Optional.of(new DelimitedRowReader(new FileInputStream(file), DelimitedTextFormat.CSV));

            case "text/tab-separated-values":
                return Optional.of(new DelimitedRowReader(new FileInputStream(file), DelimitedTextFormat.TSV));

            case "application/vnd.ms-excel":
                try {
                    return Optional.of(new ExcelRowReader(file));
                } catch (InvalidFormatException e) {
                    break;
                }

            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
            case "application/x-tika-ooxml":
                return Optional.of(OOXMLSpreadsheetRowReader.open(file));
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
        String mimeType = tika.detect(inputStream, filename);

        switch (mimeType) {
            case "text/csv":
            case "text/plain":
                return Optional.of(new DelimitedRowReader(inputStream, DelimitedTextFormat.CSV));

            case "text/tab-separated-values":
                return Optional.of(new DelimitedRowReader(inputStream, DelimitedTextFormat.TSV));

            case "application/vnd.ms-excel":
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
            case "application/x-tika-ooxml":
                try {
                    return Optional.of(new ExcelRowReader(inputStream));
                } catch (InvalidFormatException e) {
                    break;
                }
        }

        return Optional.empty();
    }

    // Apache Tika instance for detecting MIME types.
    private static final Tika tika = new Tika();
}
