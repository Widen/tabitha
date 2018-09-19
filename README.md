# Tabitha

[![Build Status](https://img.shields.io/travis/Widen/tabitha.svg)](https://travis-ci.org/Widen/tabitha)
[![Download](https://api.bintray.com/packages/widen/oss/tabitha/images/download.svg)](https://bintray.com/widen/oss/tabitha/_latestVersion)

Tabular data reading, writing, and processing library for JVM languages.

This library is currently under active development and is pre-release.

Made with :heart: by Widen.

## Features
- **Immutable data types:** Data is always immutable, which allows methods to be zero-copy operations.
- **Lazy evaluation:** Data is streamed lazily, allowing for complex data processing and producing while using constant memory.
- **Multi-threaded processing:** Input data can be consumed and processed fast and efficiently using a parallel executor.
- **Multiple formats:** Tabitha supports input and output in multiple formats, including CSV, TSV, XLSX, and XLS.

## Documentation
Documentation will be available in this repository, but is not yet written. For now, check the source for interesting comments that may help explain usage. Ideally, the class and method names should be pretty clear as to what they do.

## Examples

Reading all rows from some spreadsheet and printing out ones containing the word "happy":

```groovy
import com.widen.tabitha.reader.*

RowReaders.open("myfile.xlsx").ifPresent { RowReader reader ->
    reader.withCloseable {
        reader.rows().filter { Row row ->
            row.cells().anyMatch { Variant cell -> cell.toString() == "happy" }
        }.forEach { Row row ->
            println("Happy row: " + row)
        }
    }
}
```

Converting a simple Excel file to a CSV:

```groovy
import com.widen.tabitha.reader.*
import com.widen.tabitha.writer.*

def input = RowReaders.open("input.xls").get()
def output = RowWriters.open("output.csv").get()

try {
    input.forEach { Row row ->
        output.write(row.cells().collect(Collectors.toList()))
    }
}
finally {
    input.close()
    output.close()
}
```

## License
Available under the MIT license. See [the license file](LICENSE.md) for details.
