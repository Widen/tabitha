package com.widen.tabitha.plugins.excel;

/**
 * Helper methods for parsing Excel files.
 */
public class ParseHelpers {
    /**
     * Extract the column number from a cell name, such as "AJ23".
     *
     * @param cellName The cell name.
     * @return The column number, where "A" = 0.
     */
    public static int getColumnFromCellName(String cellName) {
        int column = 0;

        for (char c : cellName.toCharArray()) {
            if (c < 'A' || c > 'Z') {
                break;
            }

            int digit = c - 'A' + 1;
            column = (column * 26) + digit;
        }

        return column - 1;
    }
}
