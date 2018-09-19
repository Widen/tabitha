package com.widen.tabitha.formats;

import com.widen.tabitha.reader.ReaderOptions;
import com.widen.tabitha.reader.RowReader;
import com.widen.tabitha.writer.RowWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides factory methods for creating readers and writers of a particular format.
 */
public interface FormatAdapter {
    /**
     * Create a row reader for a file at the given path.
     *
     * @param path The path of the file to read.
     * @param options Options to pass to the reader.
     * @return A new row reader.
     * @throws IOException if an I/O error occurs.
     */
    default RowReader createReader(Path path, ReaderOptions options) throws IOException {
        return createReader(Files.newInputStream(path), options);
    }

    /**
     * Create a row reader for an input stream.
     *
     * @param inputStream The input stream to read.
     * @param options Options to pass to the reader.
     * @return A new row reader.
     * @throws IOException if an I/O error occurs.
     */
    RowReader createReader(InputStream inputStream, ReaderOptions options) throws IOException;

    /**
     * Create a row writer that writes to the given path.
     *
     * @param path The path to write to.
     * @return A new row writer.
     * @throws IOException if an I/O error occurs.
     */
    default RowWriter createWriter(Path path) throws IOException {
        return createWriter(Files.newOutputStream(path));
    }

    /**
     * Create a row writer that writes to the given output stream.
     *
     * @param outputStream The output stream to write to.
     * @return A new row writer.
     * @throws IOException if an I/O error occurs.
     */
    RowWriter createWriter(OutputStream outputStream) throws IOException;
}
