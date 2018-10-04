package com.widen.tabitha

import com.widen.tabitha.writer.RowWriter
import spock.lang.Specification

class RowWriterTest extends Specification {
    def "void is always writable"() {
        setup:
        100.times {
            RowWriter.VOID.write([Variant.from("hello")])
        }
    }
}
