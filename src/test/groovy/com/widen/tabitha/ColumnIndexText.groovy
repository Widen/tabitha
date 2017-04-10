package com.widen.tabitha

import spock.lang.*

class ColumnIndexText extends Specification {
    def "duplicate column names"() {
        given:
        def builder = new Schema.Builder()
        def column = "foo"
        when:
        builder.addColumn(column).addColumn(column)
        then:
        thrown RuntimeException
    }
}
