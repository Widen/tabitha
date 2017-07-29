package com.widen.tabitha.formats

import com.widen.tabitha.Helpers
import com.widen.tabitha.RowReader
import com.widen.tabitha.Variant
import spock.lang.Specification

/**
 * A base class for creating tests to prove the correctness of reading a specific format.
 */
abstract class BaseReadTest extends Specification {
    /**
     * Get the name of the test file to test agains.
     *
     * @return Name of the test file.
     */
    protected abstract String getTestFile()

    /**
     * Open a reader on a real test file.
     *
     * @return Reader to test.
     */
    protected abstract RowReader open(File file)

    /**
     * Open a reader on a real test file as a stream.
     *
     * @return Reader to test.
     */
    protected abstract RowReader open(InputStream inputStream)

    def "can open from file"() {
        setup:
        def reader = openFromFile()
        assert reader != null
        reader.close()
    }

    def "can open from stream"() {
        setup:
        def reader = openFromStream()
        assert reader != null
        reader.close()
    }

    def "contains the right contents"() {
        expect:
        reader != null

        def row = reader.read().orElse(null)
        row != null

        row.get("Column A").orElse(null) == Variant.of("foo")
        row.get("Column B").orElse(null) == Variant.of("Party")
        row.get("Column C").orElse(null) == Variant.of("Time")

        row.get(0).orElse(null) == Variant.of("foo")
        row.get(1).orElse(null) == Variant.of("Party")
        row.get(2).orElse(null) == Variant.of("Time")

        !row.get("Column Phi").isPresent()
        !row.get(3).isPresent()

        where:
        reader << [openFromFile(), openFromStream()]
    }

    private RowReader openFromFile() {
        def file = Helpers.getResourceFile(getTestFile())
        return open(file)
    }

    private RowReader openFromStream() {
        def stream = Helpers.getResourceStream(getTestFile())
        return open(stream)
    }
}
