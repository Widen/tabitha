package com.widen.tabitha.parallel

import com.widen.tabitha.Row
import com.widen.tabitha.RowReader
import com.widen.tabitha.Schema
import com.widen.tabitha.Value
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
        def schema = Schema.builder().add("column").build()
        executor.execute(new RowReader() {
            Optional<Row> read() {
                if (rowsCreated < 1000) {
                    ++rowsCreated;
                    return Optional.of(schema.createRow(Value.NONE))
                }

                return Optional.empty()
            }
        })

        then:
        rowsProcessed.get() == rowsCreated
    }
}
