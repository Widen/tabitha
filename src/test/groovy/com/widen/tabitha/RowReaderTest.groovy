package com.widen.tabitha

import com.widen.tabitha.reader.Row
import com.widen.tabitha.reader.RowReader
import spock.lang.*

class RowReaderTest extends Specification {
    def "empty reader"() {
        setup:
        def reader = RowReader.VOID

        expect:
        !reader.read().isPresent()
        !reader.read().isPresent()
    }

    def "reader from rows"() {
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

    def "reader as flowable"() {
        when:
        def row1 = Mock(Row.class)
        def row2 = Mock(Row.class)
        def row3 = Mock(Row.class)
        def rows = RowReader.from(row1, row2, row3).rows().toList().blockingGet()

        then:
        rows == [row1, row2, row3]
    }

    def "reader as iterable"() {
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
}
