package com.alibaba.fastjson2.adapter.jackson.core;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.adapter.jackson.core.util.JacksonFeature;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class JsonFactory {
    private long parserFeatures;
    private long generatorFeatures;

    public JSONWriter createJSONWriter() {
        JSONWriter jsonWriter = JSONWriter.of();
        return jsonWriter;
    }

    public boolean isCSV() {
        return false;
    }

    public JSONReader.Context createReaderContext() {
        return JSONFactory.createReadContext();
    }

    public JSONReader createJSONReader(String str) {
        JSONReader jsonReader = JSONReader.of(str, createReaderContext());
        return jsonReader;
    }

    public JSONReader createJSONReader(File str) throws IOException {
        JSONReader jsonReader = JSONReader.of(new FileInputStream(str), StandardCharsets.UTF_8, createReaderContext());
        return jsonReader;
    }

    public JsonGenerator createGenerator(OutputStream out) throws IOException {
        return createGenerator(out, JsonEncoding.UTF8);
    }

    public JsonGenerator createGenerator(OutputStream out, JsonEncoding enc)
            throws IOException {
        JSONWriter jsonWriter = createJSONWriter();
        return new JsonGenerator(jsonWriter, out, enc);
    }

    public JsonGenerator createGenerator(Writer w) throws IOException {
        JSONWriter jsonWriter = createJSONWriter();
        return new JsonGenerator(jsonWriter, w);
    }

    public JsonGenerator createGenerator(File f, JsonEncoding enc) throws IOException {
        FileOutputStream out = new FileOutputStream(f);
        return createGenerator(out, enc);
    }

    public JsonParser createParser(String content) throws IOException, JsonParseException {
        JSONReader jsonReader = JSONReader.of(content);
        return new JsonParser(jsonReader);
    }

    public JsonParser createParser(InputStream in) throws IOException, JsonParseException {
        JSONReader jsonReader = JSONReader.of(in, StandardCharsets.UTF_8);
        return new JsonParser(jsonReader);
    }

    public JsonParser createParser(Reader r) throws IOException, JsonParseException {
        JSONReader jsonReader = JSONReader.of(r);
        return new JsonParser(jsonReader);
    }

    public final JsonFactory configure(JsonParser.Feature f, boolean state) {
        return state ? enable(f) : disable(f);
    }

    public JsonFactory enable(JsonParser.Feature f) {
        parserFeatures |= f.getMask();
        return this;
    }

    public JsonFactory disable(JsonParser.Feature f) {
        parserFeatures &= ~f.getMask();
        return this;
    }

    public final JsonFactory configure(JsonGenerator.Feature f, boolean state) {
        return state ? enable(f) : disable(f);
    }

    public JsonFactory enable(JsonGenerator.Feature f) {
        generatorFeatures |= f.getMask();
        return this;
    }

    public JsonFactory disable(JsonGenerator.Feature f) {
        generatorFeatures &= ~f.getMask();
        return this;
    }

    public JsonFactory disable(Feature f) {
        return this;
    }

    public enum Feature
            implements JacksonFeature {
        INTERN_FIELD_NAMES(true),
        CANONICALIZE_FIELD_NAMES(true),
        FAIL_ON_SYMBOL_HASH_OVERFLOW(true),
        USE_THREAD_LOCAL_FOR_BUFFER_RECYCLING(true);

        private final boolean defaultState;

        Feature(boolean defaultState) { this.defaultState = defaultState; }

        public int getMask() { return (1 << ordinal()); }
    }
}
