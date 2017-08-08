package com.widen.tabitha.formats.excel;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.OutputStream;

public class XLSXRowWriter extends AbstractPOIRowWriter {
    private static final int STREAMING_WINDOW_SIZE = 1;

    /**
     * Create a new Excel row writer.
     *
     * @param output The output stream to write to.
     */
    public XLSXRowWriter(OutputStream output) {
        super(new SXSSFWorkbook(STREAMING_WINDOW_SIZE), output);
    }

    /**
     * Create a new Excel row writer.
     *
     * @param file The file to write to.
     */
    public XLSXRowWriter(File file) throws Exception {
        super(new SXSSFWorkbook(new XSSFWorkbook(file), STREAMING_WINDOW_SIZE), null);
    }
}
