package com.widen.tabitha

class ColumnIndexText {
    def "duplicate column names"() {
        given:
        def builder = new ColumnIndex.Builder()
        def column = "foo"
        when:
        builder.addColumn(column).addColumn(column)
        then:
        thrown RuntimeException
    }
}
