package com.widen.tabitha

import spock.lang.*

class HeaderTest extends Specification {
    def "build with duplicate column names"() {
        given:
        def builder = new Header.Builder()

        when:
        builder.add("foo").add("foo")

        then:
        thrown Header.DuplicateColumnException
    }

    def "get columns by name"() {
        given:
        def header = new Header.Builder()
            .add("foo")
            .add("bar")
            .add("baz")
            .build()

        expect:
        header.indexOf("foo").get() == 0
        header.indexOf("bar").get() == 1
        header.indexOf("baz").get() == 2
    }

    def "get columns by index"() {
        given:
        def header = new Header.Builder()
                .add("foo")
                .add("bar")
                .add("baz")
                .build()

        expect:
        header.nameOf(0).get() == "foo"
        header.nameOf(1).get() == "bar"
        header.nameOf(2).get() == "baz"
    }
}
