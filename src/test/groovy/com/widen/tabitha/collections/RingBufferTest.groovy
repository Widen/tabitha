package com.widen.tabitha.collections

import spock.lang.*

class RingBufferTest extends Specification {
    def buffer = new RingBuffer<String>()

    def "empty"() {
        expect:
        buffer.isEmpty()
        buffer.size() == 0
    }

    def "push and pop front"() {
        setup:
        def element = "foo"
        buffer.pushFront(element)

        expect:
        !buffer.isEmpty()
        buffer.size() == 1

        buffer.get(0) == Optional.of(element)
        buffer.popFront() == Optional.of(element)

        buffer.isEmpty()
        buffer.size() == 0
        buffer.popFront() == Optional.empty()
    }

    def "push and pop back"() {
        setup:
        def element = "foo"
        buffer.pushBack(element)

        expect:
        !buffer.isEmpty()
        buffer.size() == 1

        buffer.get(0) == Optional.of(element)
        buffer.popBack() == Optional.of(element)

        buffer.isEmpty()
        buffer.size() == 0
        buffer.popBack() == Optional.empty()
    }

    def "mix front and back operations"() {
        setup:
        def element1 = "foo"
        def element2 = "bar"
        buffer.pushBack(element2)
        buffer.pushFront(element1)

        expect:
        !buffer.isEmpty()
        buffer.size() == 2

        buffer.get(0) == Optional.of(element1)
        buffer.get(1) == Optional.of(element2)

        buffer.popBack() == Optional.of(element2)
        buffer.size() == 1

        buffer.popBack() == Optional.of(element1)
        buffer.isEmpty()
        buffer.size() == 0
    }

    def "resize preserves element order"() {
        setup:
        buffer = new RingBuffer<String>(2)

        def element1 = "foo"
        def element2 = "bar"
        buffer.pushBack(element2)
        buffer.pushFront(element1)
        buffer.pushBack(element1)

        expect:
        !buffer.isEmpty()
        buffer.size() == 3

        buffer.get(0) == Optional.of(element1)
        buffer.get(1) == Optional.of(element2)

        buffer.popBack() == Optional.of(element1)
        buffer.size() == 2

        buffer.popBack() == Optional.of(element2)
        !buffer.isEmpty()
        buffer.size() == 1
    }
}
