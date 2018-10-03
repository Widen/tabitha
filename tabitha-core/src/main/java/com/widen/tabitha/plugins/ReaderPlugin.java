package com.widen.tabitha.plugins;

import com.widen.tabitha.reader.ReaderOptions;
import com.widen.tabitha.reader.RowReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides factory methods for creating readers of a particular format.
 */
public interface ReaderPlugin extends Plugin {
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
}
