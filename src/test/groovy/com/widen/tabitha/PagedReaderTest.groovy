package com.widen.tabitha

import spock.lang.*

class PagedReaderTest extends Specification {
    def "read rows and advance pages"() {
        setup:
        def row1 = Mock(Row.class)
        def row2 = Mock(Row.class)
        def row3 = Mock(Row.class)
        def reader = createPagedReader([
                [row1],
                [row2, row3]
        ])

        expect:
        reader.read().get() == row1
        !reader.read().isPresent()
        reader.nextPage()
        reader.read().get() == row2
        reader.read().get() == row3
        !reader.read().isPresent()
        !reader.nextPage()
    }

    def "read from allPages()"() {
        setup:
        def row1 = Mock(Row.class)
        def row2 = Mock(Row.class)
        def row3 = Mock(Row.class)
        def reader = createPagedReader([
                [row1],
                [row2, row3]
        ]).mergePages()

        expect:
        reader.read().get() == row1
        reader.read().get() == row2
        reader.read().get() == row3
        !reader.read().isPresent()
    }

    private static createPagedReader(List<List<Row>> rows) {
        return new RowReader() {
            private int page = 0
            private int row = 0

            @Override
            boolean nextPage() {
                if (this.page < rows.size() - 1) {
                    this.page++
                    this.row = 0
                    return true
                }

                return false
            }

            @Override
            Optional<Row> read() throws IOException {
                if (this.row < rows[this.page].size()) {
                    return Optional.of(rows[this.page][this.row++])
                }

                return Optional.empty()
            }
        }
    }
}
