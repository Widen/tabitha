package com.widen.tabitha.formats

import com.widen.tabitha.Helpers
import com.widen.tabitha.RowReader
import com.widen.tabitha.Variant
import spock.lang.Specification

/**
 * A base class for creating tests to prove the correctness of reading a specific format.
 */
abstract class BaseReadTest extends Specification {
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

    def "reads the expected contents"() {
        expect:
        reader != null

        for (expectedRow in expectedData) {
            def row = reader.read().orElse(null)
            assert row != null
            assert row.size() == expectedRow.size()

            (0 .. row.size() - 1).each {
                assert row.get(it).orElse(null) == Variant.from(expectedRow[it])
            }

            assert !row.get(row.size()).isPresent()
        }

        assert !reader.read().isPresent()

        where:
        reader << [openFromFile(), openFromStream()]
    }

    /**
     * Get the name of the test file to test against.
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

    /**
     * Get a table of values the test file is expected to contain.
     *
     * @return Table of data.
     */
    protected abstract List<List<Object>> getExpectedData()

    private RowReader openFromFile() {
        def file = Helpers.getResourceFile(getTestFile())
        return open(file)
    }

    private RowReader openFromStream() {
        def stream = Helpers.getResourceStream(getTestFile())
        return open(stream)
    }
}
