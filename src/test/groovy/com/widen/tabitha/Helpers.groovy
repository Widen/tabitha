package com.widen.tabitha

/**
 * Helper functions for tests.
 */
class Helpers {
    public static File getResourceFile(String name) {
        return new File(getClass().getResource("/" + name).getFile())
    }

    public static InputStream getResourceStream(String name) {
        return getClass().getResourceAsStream("/" + name)
    }
}
