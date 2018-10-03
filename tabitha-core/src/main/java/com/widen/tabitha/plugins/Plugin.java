package com.widen.tabitha.plugins;

import org.atteo.classindex.IndexSubclasses;

/**
 * Base interface for a Tabitha plugin.
 * <p>
 * Tabitha plugins provide additional file formats supported though the common reader/writer interface.
 */
@IndexSubclasses
public interface Plugin {
    /**
     * Check whether this plugin supports handling files of the given MIME type.
     *
     * @param mimeType The MIME type to check support for.
     * @return True if the plugin supports the given type.
     */
    boolean supportsFormat(String mimeType);
}
