package com.alibaba.fastjson2.adapter.jackson.core;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.IOUtils;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class JsonGenerator
        implements Closeable, Flushable {
    protected final JSONWriter jsonWriter;
    protected final OutputStream out;
    protected final Charset charset;
    protected final JsonEncoding encoding;
    protected final Writer writer;

    public JsonGenerator() {
        this(JSONWriter.of(), null, JsonEncoding.UTF16_BE);
    }

    public JsonGenerator(JSONWriter jsonWriter, OutputStream out, JsonEncoding encoding) {
        this.jsonWriter = jsonWriter;
        this.out = out;
        this.encoding = encoding;
        this.charset = Charset.forName(encoding.getJavaName());
        this.writer = null;
    }

    public JsonGenerator(JSONWriter jsonWriter, Writer writer) {
        this.jsonWriter = jsonWriter;
        this.out = null;
        this.encoding = JsonEncoding.UTF16_BE;
        this.charset = StandardCharsets.UTF_16;
        this.writer = writer;
    }

    public JSONWriter getJSONWriter() {
        return jsonWriter;
    }

    public void writeNull() throws IOException {
        jsonWriter.writeNull();
    }

    public void writeRaw(String text) {
        jsonWriter.writeRaw(text);
    }

    public void writeRaw(char ch) {
        jsonWriter.writeRaw(ch);
    }

    public void flush() throws IOException {
        if (out != null) {
            jsonWriter.flushTo(out, charset);
        } else if (writer != null) {
            jsonWriter.flushTo(writer);
        }
    }

    public void writeStartObject() {
        jsonWriter.startObject();
    }

    public void writeEndObject() {
        jsonWriter.endObject();
    }

    public void writeEndArray() {
        jsonWriter.endArray();
    }

    public void writeFieldName(String name) {
        jsonWriter.writeName(name);
        jsonWriter.writeRaw(':');
    }

    public void writeString(String text) {
        jsonWriter.writeString(text);
    }

    public void writeNumber(int v) {
        jsonWriter.writeInt32(v);
    }

    public void writeNumber(BigDecimal v) {
        jsonWriter.writeDecimal(v);
    }

    public void writeNumber(long v) {
        jsonWriter.writeInt64(v);
    }

    public void writeBinary(byte[] data) {
        jsonWriter.writeBinary(data);
    }

    public void writeStringField(String fieldName, String value) throws IOException {
        writeFieldName(fieldName);
        writeString(value);
    }

    public void writeNumberField(String fieldName, BigDecimal value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    public void writeNumberField(String fieldName, long value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    public void writeNumberField(String fieldName, float value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    public void writeNumberField(String fieldName, double value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    public void writeNumberField(String fieldName, int value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    public void writeBinaryField(String fieldName, byte[] data) throws IOException {
        writeFieldName(fieldName);
        writeBinary(data);
    }

    public void writeBooleanField(String fieldName, boolean value) throws IOException {
        writeFieldName(fieldName);
        writeBoolean(value);
    }

    public void writeArrayFieldStart(String fieldName) throws IOException {
        writeFieldName(fieldName);
        writeStartArray();
    }

    public void writeNullField(String fieldName) throws IOException {
        writeFieldName(fieldName);
        writeNull();
    }

    public void writeStartArray() throws IOException {
        jsonWriter.startArray();
    }

    public void writeRawValue(String text) throws IOException {
        jsonWriter.writeRaw(text);
    }

    public void writeObject(Object pojo) throws IOException {
        jsonWriter.writeAny(pojo);
    }

    public void writeObjectField(String fieldName, Object pojo) throws IOException {
        writeFieldName(fieldName);
        writeObject(pojo);
    }

    public void writeObjectFieldStart(String fieldName) throws IOException {
        writeFieldName(fieldName);
        writeStartObject();
    }

    public void close() throws IOException {
        if (out != null) {
            jsonWriter.flushTo(out, charset);
            IOUtils.close(out);
        } else if (writer != null) {
            jsonWriter.flushTo(writer);
            IOUtils.close(writer);
        }
    }

    public enum Feature {
        // // Low-level I/O / content features

        /**
         * Feature that determines whether generator will automatically
         * close underlying output target that is NOT owned by the
         * generator.
         * If disabled, calling application has to separately
         * close the underlying {@link OutputStream} and {@link Writer}
         * instances used to create the generator. If enabled, generator
         * will handle closing, as long as generator itself gets closed:
         * this happens when end-of-input is encountered, or generator
         * is closed by a call to {@link JsonGenerator#close}.
         * <p>
         * Feature is enabled by default.
         */
        AUTO_CLOSE_TARGET(true),

        AUTO_CLOSE_JSON_CONTENT(true),

        /**
         * Feature that specifies that calls to {@link #flush} will cause
         * matching <code>flush()</code> to underlying {@link OutputStream}
         * or {@link Writer}; if disabled this will not be done.
         * Main reason to disable this feature is to prevent flushing at
         * generator level, if it is not possible to prevent method being
         * called by other code (like <code>ObjectMapper</code> or third
         * party libraries).
         * <p>
         * Feature is enabled by default.
         */
        FLUSH_PASSED_TO_STREAM(true),

        // // Quoting-related features

        @Deprecated
        QUOTE_FIELD_NAMES(true),

        @Deprecated
        QUOTE_NON_NUMERIC_NUMBERS(true),

        // // Character escaping features

        @Deprecated
        ESCAPE_NON_ASCII(false),

        // // Datatype coercion features

        @Deprecated
        WRITE_NUMBERS_AS_STRINGS(false),

        /**
         * Feature that determines whether {@link BigDecimal} entries are
         * serialized using {@link BigDecimal#toPlainString()} to prevent
         * values to be written using scientific notation.
         * <p>
         * NOTE: only affects generators that serialize {@link BigDecimal}s
         * using textual representation (textual formats but potentially some binary
         * formats).
         * <p>
         * Feature is disabled by default, so default output mode is used; this generally
         * depends on how {@link BigDecimal} has been created.
         *
         * @since 2.3
         */
        WRITE_BIGDECIMAL_AS_PLAIN(false),

        // // Schema/Validity support features

        STRICT_DUPLICATE_DETECTION(false),

        /**
         * Feature that determines what to do if the underlying data format requires knowledge
         * of all properties to output, and if no definition is found for a property that
         * caller tries to write. If enabled, such properties will be quietly ignored;
         * if disabled, a {@link JsonProcessingException} will be thrown to indicate the
         * problem.
         * Typically most textual data formats do NOT require schema information (although
         * some do, such as CSV), whereas many binary data formats do require definitions
         * (such as Avro, protobuf), although not all (Smile, CBOR, BSON and MessagePack do not).
         * <p>
         * Note that support for this feature is implemented by individual data format
         * module, if (and only if) it makes sense for the format in question. For JSON,
         * for example, this feature has no effect as properties need not be pre-defined.
         * <p>
         * Feature is disabled by default, meaning that if the underlying data format
         * requires knowledge of all properties to output, attempts to write an unknown
         * property will result in a {@link JsonProcessingException}
         *
         * @since 2.5
         */
        IGNORE_UNKNOWN(false);

        private final boolean defaultState;
        private final int mask;

        /**
         * Method that calculates bit set (flags) of all features that
         * are enabled by default.
         *
         * @return Bit field of the features that are enabled by default
         */
        public static int collectDefaults() {
            int flags = 0;
            for (Feature f : values()) {
                if (f.enabledByDefault()) {
                    flags |= f.getMask();
                }
            }
            return flags;
        }

        private Feature(boolean defaultState) {
            this.defaultState = defaultState;
            mask = (1 << ordinal());
        }

        public boolean enabledByDefault() {
            return defaultState;
        }

        // @since 2.3
        public boolean enabledIn(int flags) {
            return (flags & mask) != 0;
        }

        public int getMask() {
            return mask;
        }
    }

    public void copyCurrentStructure(JsonParser p) throws IOException {
        throw new JSONException("TODO");
    }

    public JsonGenerator setRootValueSeparator(SerializableString sep) {
        throw new UnsupportedOperationException();
    }

    public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException {
        throw new JSONException("TODO");
    }

    public int writeBinary(InputStream data, int dataLength) throws IOException {
        throw new JSONException("TODO");
    }

    public int writeBinary(Base64Variant bv, InputStream data, int dataLength) throws IOException {
        throw new JSONException("TODO");
    }

    public void writeBoolean(boolean state) throws IOException {
        jsonWriter.writeBool(state);
    }

    public void writeNumber(float v) throws IOException {
        jsonWriter.writeFloat(v);
    }

    public void writeNumber(double v) throws IOException {
        jsonWriter.writeDouble(v);
    }

    public void writeArray(int[] array, int offset, int length) throws IOException {
        throw new JSONException("TODO");
    }
}
