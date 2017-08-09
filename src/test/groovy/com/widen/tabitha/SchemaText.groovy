package com.widen.tabitha

import spock.lang.*

class SchemaText extends Specification {
    def "build with duplicate column names"() {
        given:
        def builder = new Schema.Builder()

        when:
        builder.add("foo").add("foo")

        then:
        thrown Schema.DuplicateColumnException
    }

    def "get columns by name"() {
        given:
        def schema = new Schema.Builder()
            .add("foo")
            .add("bar")
            .add("baz")
            .build()

        expect:
        schema.indexOf("foo").get() == 0
        schema.indexOf("bar").get() == 1
        schema.indexOf("baz").get() == 2
    }

    def "get columns by index"() {
        given:
        def schema = new Schema.Builder()
                .add("foo")
                .add("bar")
                .add("baz")
                .build()

        expect:
        schema.nameOf(0).get() == "foo"
        schema.nameOf(1).get() == "bar"
        schema.nameOf(2).get() == "baz"
    }
}
