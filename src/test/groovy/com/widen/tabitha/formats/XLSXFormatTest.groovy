package com.widen.tabitha.formats

class XLSXFormatTest extends BaseFormatTest {
    @Override
    FormatAdapter getAdapter() {
        return FormatRegistry.forMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").blockingGet()
    }
}
