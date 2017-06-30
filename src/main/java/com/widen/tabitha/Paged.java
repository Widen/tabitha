package com.widen.tabitha;

import java.util.Optional;

/**
 * General interface for a paged object.
 */
public interface Paged {
    /**
     * Get the index of the current page.
     *
     * @return The page index.
     */
    int getPageIndex();

    /**
     * Get the name of the current page if possible.
     *
     * @return The page name, if present.
     */
    default Optional<String> getPageName() {
        return Optional.empty();
    }
}
