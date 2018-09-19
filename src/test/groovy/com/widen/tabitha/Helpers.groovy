package com.widen.tabitha

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Helper functions for tests.
 */
class Helpers {
    public static Path getResourceFile(String name) {
        return Paths.get(getClass().getResource("/" + name).getFile())
    }

    public static InputStream getResourceStream(String name) {
        return getClass().getResourceAsStream("/" + name)
    }
}
