package com.widen.tabitha

import spock.lang.*

class VariantTest extends Specification {
    def "of factory creates correct variant types"() {
        expect:
        variant.getClass() == type

        where:
        variant            | type
        Variant.of(true)   | Variant.Bool
        Variant.of("foo")  | Variant.String
        Variant.of(42)     | Variant.Int
        Variant.of(42L)    | Variant.Int
        Variant.of(3.141)  | Variant.Float
        Variant.of(3.141D) | Variant.Float
    }

    def "of null string is none"() {
        expect:
        Variant.of(null) == Variant.NONE
    }

    def "variants unbox to correct primitives"() {
        expect:
        variant.getString().orElse(null) == stringValue.orElse(null)
        variant.getBoolean().orElse(null) == booleanValue.orElse(null)
        variant.getInteger().orElse(null) == integerValue.orElse(null)
        variant.getFloat().orElse(null) == floatValue.orElse(null)

        where:
        variant                   | booleanValue      | stringValue        | integerValue     | floatValue
        Variant.Bool.TRUE         | Optional.of(true) | Optional.empty()   | Optional.empty() | Optional.empty()
        new Variant.String("foo") | Optional.empty()  | Optional.of("foo") | Optional.empty() | Optional.empty()
        new Variant.Int(42)       | Optional.empty()  | Optional.empty()   | Optional.of(42)  | Optional.empty()
        new Variant.Float(3.141)  | Optional.empty()  | Optional.empty()   | Optional.empty() | Optional.of(3.141)
    }

    def "variant equality is reflexive and works unboxed"() {
        expect:
        boxed == boxed
        boxed == unboxed

        where:
        boxed                     | unboxed
        Variant.Bool.TRUE         | true
        new Variant.String("foo") | "foo"
        new Variant.Int(42)       | 42
        new Variant.Float(3.141)  | 3.141
    }

    def "toString correct depending on variant type"() {
        expect:
        variant.toString() == string

        where:
        variant                   | string
        Variant.Bool.TRUE         | "true"
        new Variant.String("foo") | "foo"
        new Variant.Int(42)       | "42"
        new Variant.Float(3.141)  | "3.141"
    }
}
