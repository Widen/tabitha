package com.widen.tabitha

import spock.lang.*

class RowReaderTest extends Specification {
    def "empty reader"() {
        setup:
        def reader = RowReader.EMPTY

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

    def "reader chaining"() {
        when:
        def row1 = Mock(Row.class)
        def row2 = Mock(Row.class)
        def reader1 = RowReader.from([row1])
        def reader2 = RowReader.from([row2])
        def chained = RowReader.chain(reader1, reader2)

        then:
        chained.read().get() == row1
        chained.read().get() == row2
        !chained.read().isPresent()
    }

    def "reader filtering"() {
        when:
        def row1 = Mock(Row.class)
        def row2 = Mock(Row.class)
        def row3 = Mock(Row.class)
        def reader = RowReader.from(row1, row2, row3).filter({row -> row == row2})

        then:
        reader.read().get() == row2
        !reader.read().isPresent()
    }

    def "reader mapping"() {
        when:
        def row1 = Mock(Row.class)
        def row2 = Mock(Row.class)
        def row3 = Mock(Row.class)
        def reader = RowReader.from(row1, row2).map({row -> row3})

        then:
        reader.read().get() == row3
        reader.read().get() == row3
        !reader.read().isPresent()
    }

    def "reader take"() {
        when:
        def row1 = Mock(Row.class)
        def row2 = Mock(Row.class)
        def row3 = Mock(Row.class)
        def reader = RowReader.from(row1, row2, row3).take(2)

        then:
        reader.read().get() == row1
        reader.read().get() == row2
        !reader.read().isPresent()
    }

    def "reader skip"() {
        when:
        def row1 = Mock(Row.class)
        def row2 = Mock(Row.class)
        def row3 = Mock(Row.class)
        def reader = RowReader.from(row1, row2, row3).skip(2)

        then:
        reader.read().get() == row3
        !reader.read().isPresent()
    }
}
