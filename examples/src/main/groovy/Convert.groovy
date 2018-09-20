import com.widen.tabitha.reader.Row
import com.widen.tabitha.reader.RowReaders
import com.widen.tabitha.writer.RowWriters

/**
 * Convert a simple Excel file to a CSV.
 */
class Convert {
    static main(args) {
        def input = RowReaders.open("input.xls").blockingGet()
        def output = RowWriters.create("output.csv").blockingGet()

        try {
            input.forEach { Row row ->
                output.write(row.cells())
            }
        }
        finally {
            input.close()
            output.close()
        }
    }
}
