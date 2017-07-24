package com.widen.tabitha

import spock.lang.*

class RowReaderFactoryTest extends Specification {
    def "read a CSV file"() {
        setup:
        def file = getResourceFile("Workbook1.csv")
        def reader = RowReaderFactory.open(file).orElse(null)
        assertContents(reader)
    }

    def "read a CSV stream"() {
        setup:
        def stream = getResourceStream("Workbook1.csv")
        def reader = RowReaderFactory.open(stream).orElse(null)
        assertContents(reader)
    }

    def "read an XLS file"() {
        setup:
        def file = getResourceFile("Workbook1.xls")
        def reader = RowReaderFactory.open(file).orElse(null)
        assertContents(reader)
    }

    def "read an XLS stream"() {
        setup:
        def stream = getResourceStream("Workbook1.xls")
        def reader = RowReaderFactory.open(stream).orElse(null)
        assertContents(reader)
    }

    def "read an XLSX file"() {
        setup:
        def file = getResourceFile("Workbook1.xlsx")
        def reader = RowReaderFactory.open(file).orElse(null)
        assertContents(reader)
    }

    def "read an XLSX stream"() {
        setup:
        def stream = getResourceStream("Workbook1.xlsx")
        def reader = RowReaderFactory.open(stream).orElse(null)
        assertContents(reader)
    }

    private static void assertContents(RowReader reader) {
        assert reader != null

        def row = reader.read().orElse(null)
        assert row != null

        assert row.get("Column A").orElse(null) == Variant.of("foo")
        assert row.get("Column B").orElse(null) == Variant.of("Party")
        assert row.get("Column C").orElse(null) == Variant.of("Time")

        assert row.get(0).orElse(null) == Variant.of("foo")
        assert row.get(1).orElse(null) == Variant.of("Party")
        assert row.get(2).orElse(null) == Variant.of("Time")

        assert !row.get("Column Phi").isPresent()
        assert !row.get(3).isPresent()
    }

    private static File getResourceFile(String name) {
        return new File(getClass().getResource("/" + name).getFile())
    }

    private static InputStream getResourceStream(String name) {
        return getClass().getResourceAsStream("/" + name)
    }
}
