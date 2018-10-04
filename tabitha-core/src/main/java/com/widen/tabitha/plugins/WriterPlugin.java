package com.widen.tabitha.plugins;

import com.widen.tabitha.writer.RowWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides factory methods for creating writers of a particular format.
 */
public interface WriterPlugin extends Plugin {
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
