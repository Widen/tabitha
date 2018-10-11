package com.widen.tabitha

import com.widen.tabitha.reader.Header
import spock.lang.Specification

class HeaderTest extends Specification {
    def "Build with duplicate column names"() {
        given:
        def builder = new Header.Builder()

        when:
        builder.add("foo").add("foo")

        then:
        thrown Header.DuplicateColumnException
    }

    def "Get columns by name"() {
        given:
        def header = new Header.Builder()
            .add("foo")
            .add("bar")
            .add("baz")
            .build()

        expect:
        header.indexOf("foo") == Optional.of(0)
        header.indexOf("bar") == Optional.of(1)
        header.indexOf("baz") == Optional.of(2)
    }

    def "Get columns by index"() {
        given:
        def header = new Header.Builder()
            .add("foo")
            .add("bar")
            .add("baz")
            .build()

        expect:
        header.nameOf(0) == Optional.of("foo")
        header.nameOf(1) == Optional.of("bar")
        header.nameOf(2) == Optional.of("baz")
    }

    def "Unnamed columns"() {
        given:
        def header = new Header("a", "b", null, "c", null, null)

        expect:
        header.size() == 6

        header.nameOf(0) == Optional.of("a")
        header.nameOf(1) == Optional.of("b")
        header.nameOf(2) == Optional.empty()
        header.nameOf(3) == Optional.of("c")
        header.nameOf(4) == Optional.empty()
        header.nameOf(5) == Optional.empty()

        header.indexOf("a") == Optional.of(0)
        header.indexOf("b") == Optional.of(1)
        header.indexOf("c") == Optional.of(3)
    }
}
