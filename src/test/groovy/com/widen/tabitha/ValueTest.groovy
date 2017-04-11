package com.widen.tabitha

import spock.lang.*

class ValueTest extends Specification {
    def "values unbox to correct primitives"() {
        expect:
        value.getString().orElse(null) == stringValue.orElse(null)
        value.getBoolean().orElse(null) == booleanValue.orElse(null)
        value.getInteger().orElse(null) == integerValue.orElse(null)
        value.getFloat().orElse(null) == floatValue.orElse(null)

        where:
        value                   | stringValue        | booleanValue      | integerValue     | floatValue
        new Value.String("foo") | Optional.of("foo") | Optional.empty()  | Optional.empty() | Optional.empty()
        Value.Bool.TRUE         | Optional.empty()   | Optional.of(true) | Optional.empty() | Optional.empty()
        new Value.Int(42)       | Optional.empty()   | Optional.empty()  | Optional.of(42)  | Optional.empty()
        new Value.Float(3.141)  | Optional.empty()   | Optional.empty()  | Optional.empty() | Optional.of(3.141)
    }

    def "value equality is reflexive and works unboxed"() {
        expect:
        boxed == boxed
        boxed == unboxed

        where:
        boxed                   | unboxed
        new Value.String("foo") | "foo"
        Value.Bool.TRUE         | true
        new Value.Int(42)       | 42
        new Value.Float(3.141)  | 3.141
    }

    def "toString correct depending on value type"() {
        expect:
        value.toString() == string

        where:
        value                   | string
        new Value.String("foo") | "foo"
        Value.Bool.TRUE         | "true"
        new Value.Int(42)       | "42"
        new Value.Float(3.141)  | "3.141"
    }
}
