package com.widen.tabitha.parallel

import com.widen.tabitha.Row
import com.widen.tabitha.RowReader
import com.widen.tabitha.Header
import com.widen.tabitha.Variant
import spock.lang.*

import java.util.concurrent.atomic.AtomicInteger

class ProcessorExecutorTest extends Specification {
    def "test process"() {
        setup:
        def rowsCreated = 0
        def rowsProcessed = new AtomicInteger()
        def executor = ProcessorExecutor.createDefault({ row ->
            rowsProcessed.incrementAndGet()
        })

        when:
        def header = Header.builder().add("column").build()
        executor.execute(new RowReader() {
            Optional<Row> read() {
                if (rowsCreated < 1000) {
                    ++rowsCreated;
                    return Optional.of(Row.create(Variant.NONE).withHeader(header))
                }

                return Optional.empty()
            }
        })

        then:
        rowsProcessed.get() == rowsCreated
    }
}
