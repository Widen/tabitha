package com.widen.tabitha

import com.widen.tabitha.reader.RowReaderFactory
import spock.lang.*

class RowReaderFactoryTest extends Specification {
    def "open a CSV file"() {
        setup:
        def file = Helpers.getResourceFile("Workbook1.csv")
        def reader = RowReaderFactory.open(file)

        expect:
        reader.isPresent()
    }

    def "open a CSV stream"() {
        setup:
        def stream = Helpers.getResourceStream("Workbook1.csv")
        def reader = RowReaderFactory.open(stream)

        expect:
        reader.isPresent()
    }

    def "open an XLS file"() {
        setup:
        def file = Helpers.getResourceFile("Workbook1.xls")
        def reader = RowReaderFactory.open(file)

        expect:
        reader.isPresent()
    }

    def "open an XLS stream"() {
        setup:
        def stream = Helpers.getResourceStream("Workbook1.xls")
        def reader = RowReaderFactory.open(stream)

        expect:
        reader.isPresent()
    }

    def "open an XLSX file"() {
        setup:
        def file = Helpers.getResourceFile("Workbook1.xlsx")
        def reader = RowReaderFactory.open(file)

        expect:
        reader.isPresent()
    }

    def "open an XLSX stream"() {
        setup:
        def stream = Helpers.getResourceStream("Workbook1.xlsx")
        def reader = RowReaderFactory.open(stream)

        expect:
        reader.isPresent()
    }
}
