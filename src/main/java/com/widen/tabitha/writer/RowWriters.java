package com.widen.tabitha.writer;

import com.widen.tabitha.formats.delimited.DelimitedFormat;
import com.widen.tabitha.formats.delimited.DelimitedRowWriter;
import com.widen.tabitha.formats.excel.WorkbookRowWriter;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper factory methods for creating row writers.
 */
public class RowWriters {
    /**
     * Create a new row writer for the given file path and guess the output format based on the filename.
     *
     * @param path The path to open.
     * @return A row writer for the given path.
     */
    public static RowWriter create(String path) throws IOException {
        return create(Paths.get(path));
    }

    /**
     * Create a new row writer for the given file path and guess the output format based on the filename.
     *
     * @param path The path to open.
     * @return A row writer for the given file.
     */
    public static RowWriter create(Path path) throws IOException {
        return create(Files.newOutputStream(path), path.getFileName().toString());
    }

    /**
     * Create a new row writer for the given output stream and guess the output format based on a filename.
     *
     * @return A row writer for the given output stream.
     */
    public static RowWriter create(OutputStream outputStream, String filename) {
        String extension = FilenameUtils.getExtension(filename);

        if ("xlsx".equals(extension)) {
            return WorkbookRowWriter.xlsx(outputStream);
        }

        if ("xls".equals(extension)) {
            return WorkbookRowWriter.xls(outputStream);
        }

        if ("tsv".equals(extension)) {
            return new DelimitedRowWriter(outputStream, DelimitedFormat.TSV);
        }

        return new DelimitedRowWriter(outputStream, DelimitedFormat.CSV);
    }
}
