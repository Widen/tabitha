package com.widen.tabitha.writer;

import com.widen.tabitha.formats.FormatRegistry;
import io.reactivex.Maybe;
import org.apache.tika.Tika;

import java.io.OutputStream;
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
    public static Maybe<RowWriter> create(String path) {
        return create(Paths.get(path));
    }

    /**
     * Create a new row writer for the given file path and guess the output format based on the filename.
     *
     * @param path The path to open.
     * @return A row writer for the given file.
     */
    public static Maybe<RowWriter> create(Path path) {
        return Maybe
            .fromCallable(() -> tika.detect(path))
            .flatMap(FormatRegistry::forMimeType)
            .map(formatAdapter -> formatAdapter.createWriter(path));
    }

    /**
     * Create a new row writer for the given output stream and guess the output format based on a filename.
     *
     * @param outputStream The output stream to write to.
     * @param name The name of the output file or format.
     * @return A row writer for the given output stream.
     */
    public static Maybe<RowWriter> create(OutputStream outputStream, String name) {
        return FormatRegistry
            .forMimeType(tika.detect(name))
            .map(formatAdapter -> formatAdapter.createWriter(outputStream));
    }

    // Apache Tika instance for detecting MIME types.
    private static final Tika tika = new Tika();
}
