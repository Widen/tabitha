package com.widen.tabitha.runner;

import groovy.lang.GroovyShell;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Runner {
    private static File scriptFile;

    public static void main(String[] args) throws IOException {
        for (String arg : args) {
            if (arg.equals("-h") || arg.equals("--help") || arg.equals("-?") || arg.equals("/?")) {
                printHelp();
                return;
            }
            else {
                scriptFile = new File(arg);
                break;
            }
        }

        execute();
    }

    private static void printHelp() throws IOException {
        InputStream inputStream = Runner.class.getClassLoader().getResourceAsStream("Help.txt");
        String text = IOUtils.toString(inputStream, "UTF-8");
        System.out.println(text);
    }

    private static void execute() throws IOException {
        ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStarImports("com.widen.tabitha");
        importCustomizer.addStarImports("com.widen.tabitha.formats.delimited");
        importCustomizer.addStarImports("com.widen.tabitha.formats.excel");
        importCustomizer.addStarImports("com.widen.tabitha.parallel");

        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.addCompilationCustomizers(importCustomizer);

        GroovyShell shell = new GroovyShell(configuration);
        shell.evaluate(scriptFile);
    }
}
