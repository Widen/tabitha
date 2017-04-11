package com.widen.tabitha

import spock.lang.*

class RowReaderTest extends Specification {
    def "join two readers"() {
        when:
        def left = RowReader.EMPTY
        def right = RowReader.EMPTY

        then:
        !left.zip(right).read().isPresent()
    }
}
