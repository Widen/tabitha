package com.widen.tabitha.plugins.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonStreamParser;
import com.widen.tabitha.Variant;
import com.widen.tabitha.reader.Row;
import com.widen.tabitha.reader.RowReader;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;

@AllArgsConstructor
public class JsonRowReader implements RowReader {
    private final JsonStreamParser parser;
    private int index = 0;

    public JsonRowReader(InputStream inputStream) {
        this(new InputStreamReader(inputStream));
    }

    public JsonRowReader(Reader reader) {
        parser = new JsonStreamParser(reader);
    }

    @Override
    public Optional<Row> read() {
        if (parser.hasNext()) {
            return Optional.of(Row
                .fromPairs(0, index, parser
                    .next()
                    .getAsJsonObject()
                    .entrySet()
                    .stream()
                    .map(entry -> Pair.of(entry.getKey(), createVariantFromJson(entry.getValue())))
                )
            );
        }

        return Optional.empty();
    }

    private static Variant createVariantFromJson(JsonElement jsonElement) {
        if (jsonElement.isJsonNull()) {
            return Variant.NONE;
        }

        JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();

        if (jsonPrimitive.isString()) {
            return Variant.of(jsonPrimitive.getAsString());
        }

        if (jsonPrimitive.isBoolean()) {
            return Variant.of(jsonPrimitive.getAsBoolean());
        }

        if (jsonPrimitive.isNumber()) {
            return Variant.of(jsonPrimitive.getAsNumber().doubleValue());
        }

        return Variant.NONE;
    }
}
