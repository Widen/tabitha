package com.widen.tabitha.plugins.excel

import spock.lang.Specification

class ExcelParseHelpersTest extends Specification {
    def "parse column from cell names"() {
        expect:
        ParseHelpers.getColumnFromCellName(cellName) == column

        where:
        cellName | column
        "A1"     | 0
        "B1"     | 1
        "C1"     | 2
        "C302"   | 2
        "Y12"    | 24
        "Z30"    | 25
        "AA1"    | 26
        "AB1"    | 27
        "AC1"    | 28
        "AZ1"    | 51
        "ZY1"    | 700
        "ZZ1"    | 701
        "AAA1"   | 702
    }
}
