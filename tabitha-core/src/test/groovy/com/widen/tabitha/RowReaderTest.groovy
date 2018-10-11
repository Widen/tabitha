package com.widen.tabitha

import com.widen.tabitha.reader.Row
import com.widen.tabitha.reader.RowReader
import spock.lang.Specification

class RowReaderTest extends Specification {
    def "Empty reader"() {
        setup:
        def reader = RowReader.VOID

        expect:
        !reader.read().isPresent()
        !reader.read().isPresent()
    }

    def "Reader from rows"() {
        setup:
        def row1 = Mock(Row.class)
        def row2 = Mock(Row.class)
        def row3 = Mock(Row.class)
        def reader = RowReader.from(row1, row2, row3)

        expect:
        reader.read().get() == row1
        reader.read().get() == row2
        reader.read().get() == row3
        !reader.read().isPresent()
    }

    def "Reader as flowable"() {
        when:
        def row1 = Mock(Row.class)
        def row2 = Mock(Row.class)
        def row3 = Mock(Row.class)
        def rows = RowReader.from(row1, row2, row3).rows().toList().blockingGet()

        then:
        rows == [row1, row2, row3]
    }

    def "Reader as iterable"() {
        when:
        def row1 = Mock(Row.class)
        def row2 = Mock(Row.class)
        def iterator = RowReader.from(row1, row2).iterator()

        then:
        iterator.hasNext()
        iterator.next() == row1
        iterator.hasNext()
        iterator.next() == row2
        !iterator.hasNext()
    }

    def "Sequential indexes produces expected rows"() {
        setup:
        def reader = Spy(RowReader) {
            read() >>> [
                Optional.of(new Row(0, 1, [])),
                Optional.of(new Row(0, 2, [])),
                Optional.of(new Row(0, 6, [])),
                Optional.of(new Row(3, 0, [])),
                Optional.of(new Row(3, 2, [])),
                Optional.empty(),
            ]
        }.withSequentialIndexes()

        expect:
        reader.read() == Optional.of(new Row(0, 0, []))
        reader.read() == Optional.of(new Row(0, 1, []))
        reader.read() == Optional.of(new Row(0, 2, []))
        reader.read() == Optional.of(new Row(1, 0, []))
        reader.read() == Optional.of(new Row(1, 1, []))
        reader.read() == Optional.empty()
    }

    def "Blank rows produces expected rows"() {
        setup:
        def reader = Spy(RowReader) {
            read() >>> [
                Optional.of(new Row(0, 1, [])),
                Optional.of(new Row(0, 2, [])),
                Optional.of(new Row(0, 6, [])),
                Optional.of(new Row(3, 0, [])),
                Optional.of(new Row(3, 2, [])),
                Optional.empty(),
            ]
        }.withBlankRows()

        expect:
        reader.read() == Optional.of(new Row(0, 0, []))
        reader.read() == Optional.of(new Row(0, 1, []))
        reader.read() == Optional.of(new Row(0, 2, []))
        reader.read() == Optional.of(new Row(0, 3, []))
        reader.read() == Optional.of(new Row(0, 4, []))
        reader.read() == Optional.of(new Row(0, 5, []))
        reader.read() == Optional.of(new Row(0, 6, []))
        reader.read() == Optional.of(new Row(3, 0, []))
        reader.read() == Optional.of(new Row(3, 1, []))
        reader.read() == Optional.of(new Row(3, 2, []))
        reader.read() == Optional.empty()
    }
}
