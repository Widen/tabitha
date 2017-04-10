package com.widen.tabitha

import spock.lang.*

class RowReaderTest extends Specification {
    def "join two readers"() {
        when:
        def left = new DataFrame()
        def right = new DataFrame()

        then:
        left.reader().zip(right.reader()).size() == 0
    }
}
