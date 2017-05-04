package com.widen.tabitha;

import com.widen.tabitha.formats.DelimitedRowWriter;
import com.widen.tabitha.formats.DelimitedTextFormat;
import com.widen.tabitha.formats.ExcelRowWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Helper factory methods for creating row writers.
 */
public class RowWriterFactory {
    /**
     * Create a new row writer for the given file path and guess the output format based on the filename.
     */
    public static RowWriter createWriter(String path) throws IOException {
        return createWriter(new File(path));
    }

    /**
     * Create a new row writer for the given output file and guess the output format based on the filename.
     */
    public static RowWriter createWriter(File file) throws IOException {
        OutputStream outputStream = FileUtils.openOutputStream(file);
        String filename = file.getName();

        return createWriter(outputStream, filename);
    }

    /**
     * Create a new row writer for the given output stream and guess the output format based on a filename.
     */
    public static RowWriter createWriter(OutputStream outputStream, String filename) {
        String extension = FilenameUtils.getExtension(filename);

        if ("xlsx".equals(extension)) {
            return new ExcelRowWriter(outputStream, false);
        }

        if ("xls".equals(extension)) {
            return new ExcelRowWriter(outputStream, true);
        }

        if ("tsv".equals(extension)) {
            return new DelimitedRowWriter(outputStream, DelimitedTextFormat.TSV);
        }

        return new DelimitedRowWriter(outputStream, DelimitedTextFormat.CSV);
    }
}
