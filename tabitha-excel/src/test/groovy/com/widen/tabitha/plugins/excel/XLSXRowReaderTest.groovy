package com.widen.tabitha.plugins.excel

import com.widen.tabitha.Variant
import com.widen.tabitha.reader.ReaderOptions
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class XLSXRowReaderTest extends Specification {
    def "boolean cell value #value is read correctly"() {
        given:
        def path = createWorkbook([[value]])

        when:
        def rows = XLSXRowReader.open(path, new ReaderOptions()).withCloseable { it.rows().toList().blockingGet() }

        then:
        rows.size() == 1
        rows[0].cells().size() == 1
        rows[0].cells()[0] instanceof Variant.Bool
        rows[0].cells()[0].getBoolean().get() == value

        cleanup:
        Files.deleteIfExists(path)

        where:
        value << [true, false]
    }

    def "boolean cells are read correctly alongside other cell types"() {
        given:
        def path = createWorkbook([
            [true, "true", false, 1L],
            [false, "false", true, 0L],
        ])

        when:
        def rows = XLSXRowReader.open(path, new ReaderOptions()).withCloseable { it.rows().toList().blockingGet() }

        then:
        rows.size() == 2

        rows[0].cells()*.getBoolean()*.orElse(null) == [true, null, false, null]
        rows[0].cells()[1].getString().get() == "true"
        rows[0].cells()[3].getFloat().get() == 1.0d

        rows[1].cells()*.getBoolean()*.orElse(null) == [false, null, true, null]
        rows[1].cells()[1].getString().get() == "false"
        rows[1].cells()[3].getFloat().get() == 0.0d

        cleanup:
        Files.deleteIfExists(path)
    }

    def "boolean cell values are converted to the correct string"() {
        given:
        def path = createWorkbook([[true, false]])

        when:
        def rows = XLSXRowReader.open(path, new ReaderOptions()).withCloseable { it.rows().toList().blockingGet() }

        then:
        rows[0].cells()*.toString() == ["true", "false"]

        cleanup:
        Files.deleteIfExists(path)
    }

    private static Path createWorkbook(List<List<Object>> data) {
        Path path = Files.createTempFile("tabitha-test", ".xlsx")
        new XSSFWorkbook().withCloseable { workbook ->
            def sheet = workbook.createSheet("Sheet1")
            data.eachWithIndex { rowData, rowIndex ->
                def row = sheet.createRow(rowIndex)
                rowData.eachWithIndex { value, colIndex ->
                    def cell = row.createCell(colIndex)
                    switch (value) {
                        case Boolean: cell.setCellValue((boolean) value); break
                        case Number: cell.setCellValue(((Number) value).doubleValue()); break
                        default: cell.setCellValue(value.toString())
                    }
                }
            }
            Files.newOutputStream(path).withCloseable { workbook.write(it) }
        }
        return path
    }
}
