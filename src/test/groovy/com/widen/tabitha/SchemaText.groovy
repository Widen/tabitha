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
        schema.getColumn("foo").get().name == "foo"
        schema.getColumn("bar").get().name == "bar"
        schema.getColumn("baz").get().name == "baz"
    }

    def "get columns by index"() {
        given:
        def schema = new Schema.Builder()
                .add("foo")
                .add("bar")
                .add("baz")
                .build()

        expect:
        schema.getColumn(0).get().name == "foo"
        schema.getColumn(1).get().name == "bar"
        schema.getColumn(2).get().name == "baz"
    }
}
