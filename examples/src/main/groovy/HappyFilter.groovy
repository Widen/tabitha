import com.widen.tabitha.Variant
import com.widen.tabitha.reader.Row
import com.widen.tabitha.reader.RowReader
import com.widen.tabitha.reader.RowReaders

/**
 * Read all rows from some spreadsheet and printing out ones containing the word "happy".
 */
class HappyFilter {
    static main(args) {
        RowReaders.open("myfile.xlsx").blockingGet().withCloseable { RowReader reader ->
            reader.rows().filter { Row row ->
                row.cells().any { Variant cell -> cell.toString() == "happy" }
            }.forEach { Row row ->
                println("Happy row: " + row)
            }
        }
    }
}
