package com.widen.tabitha

import spock.lang.*

class RowWriterTest extends Specification {
    def "void is always writable"() {
        setup:
        100.times {
            RowWriter.VOID.write(Mock(Row.class))
        }
    }
}
