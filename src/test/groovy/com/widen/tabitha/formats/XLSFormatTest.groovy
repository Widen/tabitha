package com.widen.tabitha.formats

class XLSFormatTest extends BaseFormatTest {
    @Override
    FormatAdapter getAdapter() {
        return FormatRegistry.forMimeType("application/vnd.ms-excel").blockingGet()
    }
}
