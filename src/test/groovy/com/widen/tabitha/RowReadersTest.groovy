package com.widen.tabitha

import com.widen.tabitha.reader.RowReaders
import spock.lang.Specification

class RowReadersTest extends Specification {
    def "open a CSV file"() {
        setup:
        def file = Helpers.getResourceFile("Workbook1.csv")
        def reader = RowReaders.open(file)

        expect:
        !reader.isEmpty().blockingGet()
    }

    def "open a CSV stream"() {
        setup:
        def stream = Helpers.getResourceStream("Workbook1.csv")
        def reader = RowReaders.open(stream)

        expect:
        !reader.isEmpty().blockingGet()
    }

    def "open an XLS file"() {
        setup:
        def file = Helpers.getResourceFile("Workbook1.xls")
        def reader = RowReaders.open(file)

        expect:
        !reader.isEmpty().blockingGet()
    }

    def "open an XLS stream"() {
        setup:
        def stream = Helpers.getResourceStream("Workbook1.xls")
        def reader = RowReaders.open(stream)

        expect:
        !reader.isEmpty().blockingGet()
    }

    def "open an XLSX file"() {
        setup:
        def file = Helpers.getResourceFile("Workbook1.xlsx")
        def reader = RowReaders.open(file)

        expect:
        !reader.isEmpty().blockingGet()
    }

    def "open an XLSX stream"() {
        setup:
        def stream = Helpers.getResourceStream("Workbook1.xlsx")
        def reader = RowReaders.open(stream)

        expect:
        !reader.isEmpty().blockingGet()
    }
}
