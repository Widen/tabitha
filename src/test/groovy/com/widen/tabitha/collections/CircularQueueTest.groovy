package com.widen.tabitha.collections

import spock.lang.*

class CircularQueueTest extends Specification {
    def "test single threaded enqueue and dequeue"() {
        setup:
        def queue = new BoundedQueue<Integer>(32)

        expect:
        queue.enqueue(1)
        queue.enqueue(2)
        queue.dequeue() == 1
        queue.dequeue() == 2
    }

    def "test enqueue and dequeue after close"() {
        setup:
        def queue = new BoundedQueue<Integer>(32)

        expect:
        queue.enqueue(1)
        queue.close()
        !queue.enqueue(2)
        queue.dequeue() == 1
        queue.dequeue() == null
        queue.isClosed()
    }
}
