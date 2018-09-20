import com.widen.tabitha.Variant
import com.widen.tabitha.reader.Row
import com.widen.tabitha.reader.RowReader
import com.widen.tabitha.reader.RowReaders
import io.reactivex.schedulers.Schedulers

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/**
 * Process a spreadsheet file in parallel and print out the number of cells containing the word "sonic".
 *
 * This is a mostly useless parallelization, since counting occurrences of a word is not CPU intensive, but you get the
 * idea at least.
 */
class ParallelProcessing {
    static void main(String[] args) {
        RowReaders.open(args[0]).blockingGet().withCloseable {
            println("# of rows: " + it.rows().count().blockingGet())
        }

        // Create a n executor with a thread pool of 64 threads.
        def executor = Executors.newFixedThreadPool(64)
        def howFast = new AtomicLong()

        // Open the file for reading.
        RowReaders.open(args[0]).blockingGet().withCloseable { RowReader reader ->
            def rows = reader.rows()

            // Subscribe to the stream and have our code run in parallel in our executor.
            rows.parallel().runOn(Schedulers.from(executor)).doOnNext { Row row ->
                row.cells().each { Variant cell ->
                    if (cell.toString().contains("sonic")) {
                        howFast.incrementAndGet()
                    }
                }
            }.sequential().blockingSubscribe() // Block until everything has been read.
        }

        // All done.
        executor.shutdown()

        println("How fast? - " + howFast.get())
    }
}
