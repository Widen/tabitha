package com.widen.tabitha;

import com.widen.tabitha.formats.DelimitedRowReader;
import com.widen.tabitha.formats.DelimitedTextFormat;
import com.widen.tabitha.formats.ExcelRowReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper factory methods for creating row readers.
 */
public class RowReaderFactory {
    /**
     * Create a new row reader for the given file path and guess the format based on the filename.
     */
    public static RowReader createReader(String path) throws IOException {
        return createReader(new File(path));
    }

    /**
     * Create a new row reader for the given input file and guess the format based on the filename.
     */
    public static RowReader createReader(File file) throws IOException {
        InputStream inputStream = FileUtils.openInputStream(file);
        String filename = file.getName();

        return createReader(inputStream, filename);
    }

    /**
     * Create a new row reader for the given input stream and guess the format based on a filename.
     */
    public static RowReader createReader(InputStream inputStream, String filename) throws IOException {
        String extension = FilenameUtils.getExtension(filename);

        if ("xlsx".equals(extension) || "xls".equals(extension)) {
            try {
                return new ExcelRowReader(inputStream);
            } catch (InvalidFormatException e) {
                // Not an Excel format.
            }
        }

        if ("tsv".equals(extension)) {
            return new DelimitedRowReader(inputStream, DelimitedTextFormat.TSV);
        }

        return new DelimitedRowReader(inputStream, DelimitedTextFormat.CSV);
    }
}
