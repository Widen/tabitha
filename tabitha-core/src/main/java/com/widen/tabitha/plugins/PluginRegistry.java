package com.widen.tabitha.plugins;

import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;
import org.atteo.classindex.ClassIndex;

import java.lang.reflect.Modifier;

/**
 * Manages the adapters for the file formats supported by Tabitha.
 * <p>
 * You probably want to use {@link com.widen.tabitha.reader.RowReaders} or {@link com.widen.tabitha.writer.RowWriter}
 * instead.
 */
@Slf4j
public class PluginRegistry {
    /**
     * Get all available plugins.
     *
     * @return Observable of loaded plugins.
     */
    public static Observable<Plugin> getPlugins() {
        return PLUGINS;
    }

    /**
     * Get available reader plugins.
     *
     * @return Observable of loaded plugins.
     */
    public static Observable<ReaderPlugin> getReaderPlugins() {
        return getPlugins().ofType(ReaderPlugin.class);
    }

    /**
     * Get available writer plugins.
     *
     * @return Observable of loaded plugins.
     */
    public static Observable<WriterPlugin> getWriterPlugins() {
        return getPlugins().ofType(WriterPlugin.class);
    }

    private static Observable<Plugin> PLUGINS = Observable
        // Get all plugin implementations.
        .fromIterable(ClassIndex.getSubclasses(Plugin.class))
        // Only include instantiable classes.
        .filter(pluginClass -> !pluginClass.isInterface())
        .filter(pluginClass -> !Modifier.isAbstract(pluginClass.getModifiers()))
        // Lazily instantiate them.
        .map(Class::newInstance)
        .cast(Plugin.class)
        .doOnNext(formatAdapter -> log.debug("Loaded plugin: {}", formatAdapter.getClass().getName()))
        .cache();
}
