package com.widen.tabitha

import spock.lang.*

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
            writer.write(Mock(Row.class))
        }

        then:
        3 * pagedWriter.beginPage() >> null
    }
}
