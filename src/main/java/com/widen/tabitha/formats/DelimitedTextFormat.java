package com.widen.tabitha.formats;

/**
 * Format options for a delimiter-separated text file.
 */
public class DelimitedTextFormat
{
    /**
     * Delimited text format for a CSV file.
     */
    public static final DelimitedTextFormat CSV = new DelimitedTextFormat(',', '"', '"', false);

    /**
     * Delimited text format for a TSV file.
     */
    public static final DelimitedTextFormat TSV = new DelimitedTextFormat('\t', '"', '\\', false);

    private final char delimiter;
    private final char quoteCharacter;
    private final char escapeCharacter;
    private final boolean strictQuotes;

    public DelimitedTextFormat(char delimiter, char quoteCharacter, char escapeCharacter, boolean strictQuotes)
    {
        this.delimiter = delimiter;
        this.quoteCharacter = quoteCharacter;
        this.escapeCharacter = escapeCharacter;
        this.strictQuotes = strictQuotes;
    }

    public char getDelimiter()
    {
        return delimiter;
    }

    public char getQuoteCharacter()
    {
        return quoteCharacter;
    }

    public char getEscapeCharacter()
    {
        return escapeCharacter;
    }

    public boolean isStrictQuotes()
    {
        return strictQuotes;
    }

    public DelimitedTextFormat withDelimiter(char delimiter)
    {
        return new DelimitedTextFormat(delimiter, quoteCharacter, escapeCharacter, strictQuotes);
    }

    public DelimitedTextFormat withQuoteCharacter(char quoteCharacter)
    {
        return new DelimitedTextFormat(delimiter, quoteCharacter, escapeCharacter, strictQuotes);
    }

    public DelimitedTextFormat withEscapeCharacter(char escapeCharacter)
    {
        return new DelimitedTextFormat(delimiter, quoteCharacter, escapeCharacter, strictQuotes);
    }

    public DelimitedTextFormat withStrictQuotes(boolean strictQuotes)
    {
        return new DelimitedTextFormat(delimiter, quoteCharacter, escapeCharacter, strictQuotes);
    }
}
