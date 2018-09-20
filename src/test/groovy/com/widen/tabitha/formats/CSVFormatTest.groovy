package com.widen.tabitha.formats

class CSVFormatTest extends BaseFormatTest {
    @Override
    FormatAdapter getAdapter() {
        return FormatRegistry.forMimeType("text/csv").blockingGet()
    }
}
