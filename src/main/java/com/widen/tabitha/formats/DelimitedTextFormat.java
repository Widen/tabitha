package com.widen.tabitha.formats;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Format options for a delimiter-separated text file.
 */
public class DelimitedTextFormat {
    /**
     * Delimited text format for a CSV file.
     */
    public static final DelimitedTextFormat CSV = new DelimitedTextFormat(',', '"', '\\', false, StandardCharsets.UTF_8);

    /**
     * Delimited text format for a TSV file.
     */
    public static final DelimitedTextFormat TSV = new DelimitedTextFormat('\t', '"', '\\', false, StandardCharsets.UTF_8);

    public final char delimiter;
    public final char quoteCharacter;
    public final char escapeCharacter;
    public final boolean strictQuotes;
    public final Charset charset;

    public DelimitedTextFormat(char delimiter, char quoteCharacter, char escapeCharacter, boolean strictQuotes, Charset charset) {
        this.delimiter = delimiter;
        this.quoteCharacter = quoteCharacter;
        this.escapeCharacter = escapeCharacter;
        this.strictQuotes = strictQuotes;
        this.charset = charset;
    }

    public DelimitedTextFormat withDelimiter(char delimiter) {
        return new DelimitedTextFormat(delimiter, quoteCharacter, escapeCharacter, strictQuotes, charset);
    }

    public DelimitedTextFormat withQuoteCharacter(char quoteCharacter) {
        return new DelimitedTextFormat(delimiter, quoteCharacter, escapeCharacter, strictQuotes, charset);
    }

    public DelimitedTextFormat withEscapeCharacter(char escapeCharacter) {
        return new DelimitedTextFormat(delimiter, quoteCharacter, escapeCharacter, strictQuotes, charset);
    }

    public DelimitedTextFormat withStrictQuotes(boolean strictQuotes) {
        return new DelimitedTextFormat(delimiter, quoteCharacter, escapeCharacter, strictQuotes, charset);
    }

    public DelimitedTextFormat withCharset(Charset charset) {
        return new DelimitedTextFormat(delimiter, quoteCharacter, escapeCharacter, strictQuotes, charset);
    }
}
