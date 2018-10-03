package com.widen.tabitha.plugins.delimited;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Format options for a delimiter-separated text file.
 */
public class DelimitedFormat {
    /**
     * Delimited text format for a CSV file.
     */
    public static final DelimitedFormat CSV = new DelimitedFormat(',', '"', '\\', false, StandardCharsets.UTF_8);

    /**
     * Delimited text format for a TSV file.
     */
    public static final DelimitedFormat TSV = new DelimitedFormat('\t', '"', '\\', false, StandardCharsets.UTF_8);

    /**
     * The delimiter to use for separating entries.
     */
    public final char delimiter;

    /**
     * The character to use for quoted elements.
     */
    public final char quoteCharacter;

    /**
     * The character to use for escaping a separator or quote.
     */
    public final char escapeCharacter;

    /**
     * Whether characters outside quotes are ignored.
     */
    public final boolean strictQuotes;

    /**
     * The character encoding of the text.
     */
    public final Charset charset;

    /**
     * Create a new delimited format.
     *
     * @param delimiter The delimiter to use for separating entries.
     * @param quoteCharacter The character to use for quoted elements.
     * @param escapeCharacter The character to use for escaping a separator or quote.
     * @param strictQuotes Whether characters outside quotes are ignored.
     * @param charset The character encoding of the text.
     */
    public DelimitedFormat(char delimiter, char quoteCharacter, char escapeCharacter, boolean strictQuotes, Charset charset) {
        this.delimiter = delimiter;
        this.quoteCharacter = quoteCharacter;
        this.escapeCharacter = escapeCharacter;
        this.strictQuotes = strictQuotes;
        this.charset = charset;
    }

    /**
     * Return a new format with the given delimiter.
     *
     * @param delimiter The delimiter to use for separating entries.
     * @return The new format.
     */
    public DelimitedFormat withDelimiter(char delimiter) {
        return new DelimitedFormat(delimiter, quoteCharacter, escapeCharacter, strictQuotes, charset);
    }

    /**
     * Return a new format with the given quote character.
     *
     * @param quoteCharacter The character to use for quoted elements.
     * @return The new format.
     */
    public DelimitedFormat withQuoteCharacter(char quoteCharacter) {
        return new DelimitedFormat(delimiter, quoteCharacter, escapeCharacter, strictQuotes, charset);
    }

    /**
     * Return a new format with the given escape character.
     *
     * @param escapeCharacter The character to use for escaping a separator or quote.
     * @return The new format.
     */
    public DelimitedFormat withEscapeCharacter(char escapeCharacter) {
        return new DelimitedFormat(delimiter, quoteCharacter, escapeCharacter, strictQuotes, charset);
    }

    /**
     * Return a new format with the given strict quoting policy.
     *
     * @param strictQuotes Whether characters outside quotes are ignored.
     * @return The new format.
     */
    public DelimitedFormat withStrictQuotes(boolean strictQuotes) {
        return new DelimitedFormat(delimiter, quoteCharacter, escapeCharacter, strictQuotes, charset);
    }

    /**
     * Return a new format with the given character encoding.
     *
     * @param charset The character encoding of the text.
     * @return The new format.
     */
    public DelimitedFormat withCharset(Charset charset) {
        return new DelimitedFormat(delimiter, quoteCharacter, escapeCharacter, strictQuotes, charset);
    }
}
