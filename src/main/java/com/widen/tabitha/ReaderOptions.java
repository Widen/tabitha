package com.widen.tabitha;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

@Getter
@Wither
@AllArgsConstructor
public class ReaderOptions {
    /**
     * Interpret the first row of each page of data as a header. This is only applicable to formats that do not have a
     * built-in concept of column headers.
     */
    private final boolean inlineHeaders;

    /**
     * Whether hidden rows should produced by the reader.
     */
    private final boolean includeHiddenRows;

    /**
     * Whether hidden cells should produced by the reader. If disabled (the default), hidden cells will be replaced with
     * {@link Variant#NONE}.
     */
    private final boolean includeHiddenCells;

    /**
     * Create a new {@link ReaderOptions} with the default values set.
     */
    public ReaderOptions() {
        this(true, false, false);
    }
}
