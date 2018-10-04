package com.widen.tabitha

import com.widen.tabitha.plugins.Plugin
import com.widen.tabitha.plugins.PluginRegistry
import com.widen.tabitha.plugins.ReaderPlugin
import com.widen.tabitha.plugins.WriterPlugin
import com.widen.tabitha.reader.ReaderOptions
import com.widen.tabitha.reader.Row
import com.widen.tabitha.reader.RowReader
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

/**
 * A base class for creating tests to prove the correctness of reading a specific format.
 */
class FormatTest extends Specification {
    static options = new ReaderOptions().withInlineHeaders(false)

    def "Can read from file"() {
        setup:
        def file = fixture(plugin as WriterPlugin, [])

        when:
        def reader = (plugin as ReaderPlugin).createReader(file, options)

        then:
        reader != null
        reader.close()

        cleanup:
        Files.delete(file)

        where:
        plugin << testablePlugins.toList().blockingGet()
    }

    def "Can read from stream"() {
        setup:
        def file = fixture(plugin as WriterPlugin, [])

        when:
        def reader = (plugin as ReaderPlugin).createReader(file, options)

        then:
        reader != null
        reader.close()

        cleanup:
        Files.delete(file)

        where:
        plugin << testablePlugins.toList().blockingGet()
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
        def file = fixture(plugin as WriterPlugin, expectedData)
        def reader = (plugin as ReaderPlugin).createReader(file, options)

        when:
        def actualData = readAllData(reader)

        then:
        actualData == expectedData

        cleanup:
        reader.close()
        Files.delete(file)

        where:
        plugin << PluginRegistry.readerPlugins.toList().blockingGet()
    }

    private static Path fixture(WriterPlugin plugin, List<List<List<Variant>>> pages) {
        def path = Files.createTempFile(null, null)

        plugin.createWriter(path).withCloseable { writer ->
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

    private static io.reactivex.Observable<Plugin> getTestablePlugins() {
        return PluginRegistry.plugins.ofType(ReaderPlugin).ofType(WriterPlugin)
    }
}
