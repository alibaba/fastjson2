package com.fasterxml.jackson.core;

import com.alibaba.fastjson2.JSONWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;

public class JsonGeneratorWrapper extends JsonGenerator {
    private final JSONWriter jsonWriter;
    private final OutputStream out;
    private final JsonEncoding encoding;
    private final Charset charset;

    public JsonGeneratorWrapper(JSONWriter jsonWriter) {
        this(jsonWriter, null, JsonEncoding.UTF8);
    }

    public JsonGeneratorWrapper(JSONWriter jsonWriter, OutputStream out, JsonEncoding encoding) {
        this.jsonWriter = jsonWriter;
        this.out = out;
        this.encoding = encoding;
        this.charset = Charset.forName(encoding.getJavaName());
    }

    @Override
    public void writeRaw(String text) {
        jsonWriter.writeRaw(text);
    }

    @Override
    public void flush() throws IOException {
        jsonWriter.flushTo(out, charset);
    }

    @Override
    public void writeStartObject() {
        jsonWriter.startObject();
    }

    @Override
    public void writeEndObject() {
        jsonWriter.endObject();
    }

    @Override
    public void writeEndArray() {
        jsonWriter.endArray();
    }

    @Override
    public void writeFieldName(String name) {
        jsonWriter.writeName(name);
        jsonWriter.writeRaw(':');
    }

    @Override
    public void writeString(String text) {
        jsonWriter.writeString(text);
    }

    @Override
    public void writeNumber(int v) throws IOException {
        jsonWriter.writeInt32(v);
    }

    @Override
    public void writeNumber(long v) throws IOException {
        jsonWriter.writeInt64(v);
    }

    @Override
    public void writeNumber(BigDecimal v) throws IOException {
        jsonWriter.writeDecimal(v);
    }

    @Override
    public void close() throws IOException {
        jsonWriter.flushTo(out, charset);
    }
}
