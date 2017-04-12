package com.widen.tabitha

import spock.lang.*

class VariantTest extends Specification {
    def "variants unbox to correct primitives"() {
        expect:
        value.getString().orElse(null) == stringValue.orElse(null)
        value.getBoolean().orElse(null) == booleanValue.orElse(null)
        value.getInteger().orElse(null) == integerValue.orElse(null)
        value.getFloat().orElse(null) == floatValue.orElse(null)

        where:
        value                     | stringValue        | booleanValue      | integerValue     | floatValue
        new Variant.String("foo") | Optional.of("foo") | Optional.empty()  | Optional.empty() | Optional.empty()
        Variant.Bool.TRUE         | Optional.empty()   | Optional.of(true) | Optional.empty() | Optional.empty()
        new Variant.Int(42)       | Optional.empty()   | Optional.empty()  | Optional.of(42)  | Optional.empty()
        new Variant.Float(3.141)  | Optional.empty()   | Optional.empty()  | Optional.empty() | Optional.of(3.141)
    }

    def "variant equality is reflexive and works unboxed"() {
        expect:
        boxed == boxed
        boxed == unboxed

        where:
        boxed                     | unboxed
        new Variant.String("foo") | "foo"
        Variant.Bool.TRUE         | true
        new Variant.Int(42)       | 42
        new Variant.Float(3.141)  | 3.141
    }

    def "toString correct depending on variant type"() {
        expect:
        value.toString() == string

        where:
        value                     | string
        new Variant.String("foo") | "foo"
        Variant.Bool.TRUE         | "true"
        new Variant.Int(42)       | "42"
        new Variant.Float(3.141)  | "3.141"
    }
}
