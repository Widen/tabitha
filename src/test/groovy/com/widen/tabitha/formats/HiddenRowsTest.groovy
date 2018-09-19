package com.widen.tabitha.formats

import com.widen.tabitha.Helpers
import com.widen.tabitha.reader.ReaderOptions
import com.widen.tabitha.reader.RowReaders
import spock.lang.Specification

class HiddenRowsTest extends Specification {
    def "Hidden rows are ignored"() {
        setup:
        def reader = RowReaders.open(
            Helpers.getResourceStream(file),
            new ReaderOptions().withIncludeHiddenRows(false)
        ).blockingGet()

        expect:
        reader.each { row ->
            assert !row.toString().contains("hidden")
        }

        where:
        file << ["HiddenRows.xlsx"]
    }

    def "Hidden rows are not ignored"() {
        setup:
        def reader = RowReaders.open(
            Helpers.getResourceStream(file),
            new ReaderOptions().withIncludeHiddenRows(true)
        ).blockingGet()

        when:
        def foundHidden = false
        reader.each { row ->
            if (row.toString().contains("hidden")) {
                foundHidden = true
            }
        }

        then:
        foundHidden

        where:
        file << ["HiddenRows.xlsx"]
    }
}
