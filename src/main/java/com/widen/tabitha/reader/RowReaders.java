package com.widen.tabitha.reader;

import com.widen.tabitha.formats.FormatRegistry;
import io.reactivex.Maybe;
import org.apache.tika.Tika;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    public static Maybe<RowReader> open(String path) {
        return open(Paths.get(path), null);
    }

    /**
     * Attempt to detect the format of a file at the given path and open it as a row reader.
     *
     * @param path The file path of the file to open.
     * @return A row reader if the file is in a supported format.
     */
    public static Maybe<RowReader> open(Path path) {
        return open(path, null);
    }

    /**
     * Attempt to detect the format of a file at the given path and open it as a row reader.
     *
     * @param path The file path of the file to open.
     * @param options Options to pass to the reader.
     * @return A row reader if the file is in a supported format.
     */
    public static Maybe<RowReader> open(Path path, ReaderOptions options) {
        return Maybe
            .fromCallable(() -> tika.detect(path))
            .flatMap(FormatRegistry::forMimeType)
            .map(formatAdapter -> formatAdapter.createReader(path, options != null ? options : new ReaderOptions()));
    }

    /**
     * Attempt to detect the format of an input stream and open it as a row reader.
     *
     * @param inputStream The input stream to read.
     * @return A row reader if the stream is in a supported format.
     */
    public static Maybe<RowReader> open(InputStream inputStream) {
        return open(inputStream, null, null);
    }

    /**
     * Attempt to detect the format of an input stream and open it as a row reader.
     *
     * @param inputStream The input stream to read.
     * @param filename The filename associated with the stream, if known.
     * @return A row reader if the stream is in a supported format.
     */
    public static Maybe<RowReader> open(InputStream inputStream, String filename) {
        return open(inputStream, filename, null);
    }

    /**
     * Attempt to detect the format of an input stream and open it as a row reader.
     *
     * @param inputStream The input stream to read.
     * @param options Options to pass to the reader.
     * @return A row reader if the stream is in a supported format.
     */
    public static Maybe<RowReader> open(InputStream inputStream, ReaderOptions options) {
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
    public static Maybe<RowReader> open(InputStream inputStream, String filename, ReaderOptions options) {
        // If our input stream supports marks, Tika will rewind the stream back to the start for us after detecting the
        // format, so ensure our input stream supports it.
        InputStream rewindableStream = createRewindableInputStream(inputStream);

        return Maybe
            .fromCallable(() -> tika.detect(rewindableStream, filename))
            .flatMap(FormatRegistry::forMimeType)
            .map(formatAdapter -> formatAdapter.createReader(rewindableStream, options != null ? options : new ReaderOptions()));
    }

    private static InputStream createRewindableInputStream(InputStream inputStream) {
        return inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
    }

    // Apache Tika instance for detecting MIME types.
    private static final Tika tika = new Tika();
}
