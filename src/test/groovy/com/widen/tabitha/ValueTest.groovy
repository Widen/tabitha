package com.widen.tabitha

import spock.lang.*

class ValueTest extends Specification {
    def "values unbox to correct primitives"() {
        expect:
        value.stringValue().orElse(null) == stringValue.orElse(null)
        value.booleanValue().orElse(null) == booleanValue.orElse(null)
        value.integerValue().orElse(null) == integerValue.orElse(null)
        value.floatValue().orElse(null) == floatValue.orElse(null)

        where:
        value                   | stringValue        | booleanValue      | integerValue     | floatValue
        new Value.String("foo") | Optional.of("foo") | Optional.empty()  | Optional.empty() | Optional.empty()
        Value.Bool.True         | Optional.empty()   | Optional.of(true) | Optional.empty() | Optional.empty()
        new Value.Int(42)       | Optional.empty()   | Optional.empty()  | Optional.of(42)  | Optional.empty()
        new Value.Float(3.141)  | Optional.empty()   | Optional.empty()  | Optional.empty() | Optional.of(3.141)
    }

    def "asString correct depending on value type"() {
        expect:
        value.asString() == string

        where:
        value                   | string
        new Value.String("foo") | "foo"
        Value.Bool.True         | "true"
        new Value.Int(42)       | "42"
        new Value.Float(3.141)  | "3.141"
    }
}
