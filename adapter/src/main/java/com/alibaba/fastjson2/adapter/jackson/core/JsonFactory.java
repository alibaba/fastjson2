package com.alibaba.fastjson2.adapter.jackson.core;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.adapter.jackson.databind.JsonParserWrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class JsonFactory {
    private long parserFeatures;
    private long generatorFeatures;

    public JsonGenerator createGenerator(OutputStream out, JsonEncoding enc)
            throws IOException {
        JSONWriter jsonWriter = JSONWriter.of();
        return new JsonGeneratorWrapper(jsonWriter, out, enc);
    }

    public JsonGenerator createGenerator(File f, JsonEncoding enc) throws IOException {
        FileOutputStream out = new FileOutputStream(f);
        return createGenerator(out, enc);
    }

    public JsonParser createParser(String content) throws IOException, JsonParseException {
        JSONReader jsonReader = JSONReader.of(content);
        return new JsonParserWrapper(jsonReader);
    }

    public final JsonFactory configure(JsonParser.Feature f, boolean state) {
        return state ? enable(f) : disable(f);
    }

    public JsonFactory enable(JsonParser.Feature f) {
        parserFeatures |= f.getMask();
        return this;
    }

    /**
     * Method for disabling specified parser features
     * (check {@link JsonParser.Feature} for list of features)
     *
     * @param f Feature to disable
     * @return This factory instance (to allow call chaining)
     */
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

    /**
     * Method for disabling specified generator feature
     * (check {@link JsonGenerator.Feature} for list of features)
     *
     * @param f Feature to disable
     * @return This factory instance (to allow call chaining)
     */
    public JsonFactory disable(JsonGenerator.Feature f) {
        generatorFeatures &= ~f.getMask();
        return this;
    }
}
