package com.widen.tabitha

import spock.lang.*

class DataFrameTest extends Specification {
    def "test is streaming"() {
        setup:
        def inMemory = new DataFrame()
        def streaming = DataFrame.streaming(RowReader.VOID)

        expect:
        !inMemory.isStreaming()
        streaming.isStreaming()
    }

    def "test push and pop"() {
        setup:
        def row = Mock(Row.class)
        def dataFrame = new DataFrame()

        expect:
        !dataFrame.popFront().isPresent()
        dataFrame.pushBack(row)
        dataFrame.popFront().get() == row
        !dataFrame.popFront().isPresent()
    }

    def "test reading from a frame"() {
        setup:
        def row1 = Mock(Row.class)
        def row2 = Mock(Row.class)
        def row3 = Mock(Row.class)
        def dataFrame = new DataFrame(row1, row2, row3)
        def reader = dataFrame.reader();

        expect:
        reader.read().get() == row1
        reader.read().get() == row2
        reader.read().get() == row3
        !reader.read().isPresent()
    }

    def "test writing to a frame"() {
        setup:
        def row1 = Mock(Row.class)
        def row2 = Mock(Row.class)
        def row3 = Mock(Row.class)
        def dataFrame = new DataFrame()
        def writer = dataFrame.writer();

        expect:
        dataFrame.isEmpty()

        writer.write(row1)
        dataFrame.get(0).get() == row1

        writer.write(row2)
        dataFrame.get(1).get() == row2

        writer.write(row3)
        dataFrame.get(2).get() == row3
    }

    def "test streaming reads automatically"() {
        setup:
        def row1 = Mock(Row.class)
        def row2 = Mock(Row.class)
        def row3 = Mock(Row.class)
        def row4 = Mock(Row.class)
        def dataFrame = DataFrame.streaming(RowReader.from(row1, row2, row3, row4))

        expect:
        dataFrame.size() == 0
        dataFrame.isEmpty()

        dataFrame.popFront().get() == row1

        dataFrame.size() == 0
        dataFrame.isEmpty()

        dataFrame.popBack().get() == row2

        dataFrame.size() == 0
        dataFrame.isEmpty()

        dataFrame.get(0).get() == row3

        dataFrame.size() == 1
        !dataFrame.isEmpty()

        dataFrame.popFront().get() == row3

        dataFrame.size() == 0
        dataFrame.isEmpty()

        dataFrame.close()
        !dataFrame.get(0).isPresent()
    }
}
