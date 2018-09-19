package com.widen.tabitha

import com.widen.tabitha.writer.PagedWriter
import spock.lang.Specification

class PagedWriterTest extends Specification {
    def partitioned() {
        setup:
        PagedWriter pagedWriter = Spy() {
            write(_) >> null
            beginPage() >> null
        }
        def writer = pagedWriter.partitioned(100)

        when:
        301.times {
            writer.write([Variant.from("hello")])
        }

        then:
        3 * pagedWriter.beginPage() >> null
    }
}
