package com.widen.tabitha.formats

import com.widen.tabitha.Variant
import com.widen.tabitha.reader.ReaderOptions
import com.widen.tabitha.reader.Row
import com.widen.tabitha.reader.RowReader
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

/**
 * A base class for creating tests to prove the correctness of reading a specific format.
 */
abstract class BaseFormatTest extends Specification {
    static options = new ReaderOptions().withInlineHeaders(false)

    abstract FormatAdapter getAdapter()

    def "Can read from file"() {
        setup:
        def file = fixture([])

        when:
        def reader = adapter.createReader(file, options)

        then:
        reader != null
        reader.close()

        cleanup:
        Files.delete(file)
    }

    def "Can read from stream"() {
        setup:
        def file = fixture([])

        when:
        def reader = adapter.createReader(file.newInputStream(), options)

        then:
        reader != null
        reader.close()

        cleanup:
        Files.delete(file)
    }

    def "Read-write round trip"() {
        setup:
        def expectedData = asVariants([
            [
                ["Column A", "Column B", "Column C"],
                ["foo", "Party", "Time"],
                ["bar", null, "World"],
                ["baz", "Excel", "10009"],
            ]
        ])
        def file = fixture(expectedData)
        def reader = adapter.createReader(file, options)

        when:
        def actualData = readAllData(reader)

        then:
        actualData == expectedData

        cleanup:
        reader.close()
        Files.delete(file)
    }

    private Path fixture(List<List<List<Variant>>> pages) {
        def path = Files.createTempFile(null, null)

        adapter.createWriter(path).withCloseable { writer ->
            pages.each { rows ->
                rows.each { cells ->
                    writer.write(cells)
                }
            }
        }

        return path
    }

    private static List<List<List<Variant>>> asVariants(List<List<List<Object>>> pages) {
        return pages.collect {
            it.collect {
                it.collect {
                    Variant.from(it)
                }
            }
        }
    }

    private static List<List<List<Variant>>> readAllData(RowReader reader) {
        return reader
            .rows()
            .groupBy({ Row row -> row.page() })
            .flatMap({ it.map { Row row -> row.cells() }.toList().toFlowable() })
            .toList()
            .blockingGet()
    }
}
