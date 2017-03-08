package com.widen.tabitha.collections

import spock.lang.*

class RingBufferTest extends Specification {
    def buffer = new RingBuffer<String>()

    def "empty"() {
        setup:
        assert buffer.isEmpty()
        assert buffer.size() == 0
    }

    def "push and pop front"() {
        setup:
        def element = "foo"
        buffer.pushFront(element)

        assert !buffer.isEmpty()
        assert buffer.size() == 1

        assert buffer.get(0) == Optional.of(element)
        assert buffer.popFront() == Optional.of(element)

        assert buffer.isEmpty()
        assert buffer.size() == 0
        assert buffer.popFront() == Optional.empty()
    }

    def "push and pop back"() {
        setup:
        def element = "foo"
        buffer.pushBack(element)

        assert !buffer.isEmpty()
        assert buffer.size() == 1

        assert buffer.get(0) == Optional.of(element)
        assert buffer.popBack() == Optional.of(element)

        assert buffer.isEmpty()
        assert buffer.size() == 0
        assert buffer.popBack() == Optional.empty()
    }

    def "mix front and back operations"() {
        setup:
        def element1 = "foo"
        def element2 = "bar"
        buffer.pushBack(element2)
        buffer.pushFront(element1)

        assert !buffer.isEmpty()
        assert buffer.size() == 2

        assert buffer.get(0) == Optional.of(element1)
        assert buffer.get(1) == Optional.of(element2)

        assert buffer.popBack() == Optional.of(element2)
        assert buffer.size() == 1

        assert buffer.popBack() == Optional.of(element1)
        assert buffer.isEmpty()
        assert buffer.size() == 0
    }

    def "resize preserves element order"() {
        setup:
        buffer = new RingBuffer<String>(2)

        def element1 = "foo"
        def element2 = "bar"
        buffer.pushBack(element2)
        buffer.pushFront(element1)
        buffer.pushBack(element1)

        assert !buffer.isEmpty()
        assert buffer.size() == 3

        assert buffer.get(0) == Optional.of(element1)
        assert buffer.get(1) == Optional.of(element2)

        assert buffer.popBack() == Optional.of(element1)
        assert buffer.size() == 2

        assert buffer.popBack() == Optional.of(element2)
        assert !buffer.isEmpty()
        assert buffer.size() == 1
    }
}
