package com.widen.tabitha

import spock.lang.*

class RowWriterTest extends Specification {
    def "null is always writable"() {
        setup:
        100.times {
            RowWriter.NULL.write(Mock(Row.class))
        }
    }
}
