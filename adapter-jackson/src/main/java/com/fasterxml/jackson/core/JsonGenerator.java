package com.fasterxml.jackson.core;

import com.alibaba.fastjson2.JSONWriter;

import java.io.*;
import java.math.BigDecimal;

public abstract class JsonGenerator
        implements Closeable, Flushable {
    private JSONWriter raw;

    public abstract void writeRaw(String text) throws IOException;

    @Override
    public abstract void flush() throws IOException;

    public abstract void writeStartObject();

    public abstract void writeEndObject();

    public abstract void writeEndArray();

    public abstract void writeFieldName(String name);

    public abstract void writeString(String text);

    public abstract void writeNumber(int v) throws IOException;

    public abstract void writeNumber(long v) throws IOException;

    public abstract void writeNumber(BigDecimal v) throws IOException;

    public void writeStringField(String fieldName, String value) throws IOException {
        writeFieldName(fieldName);
        writeString(value);
    }

    public void writeNumberField(String fieldName, BigDecimal value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    public void writeNumberField(String fieldName, int value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
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
         * Feature that determines whether {@link java.math.BigDecimal} entries are
         * serialized using {@link java.math.BigDecimal#toPlainString()} to prevent
         * values to be written using scientific notation.
         * <p>
         * NOTE: only affects generators that serialize {@link java.math.BigDecimal}s
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
        IGNORE_UNKNOWN(false),
        ;

        private final boolean _defaultState;
        private final int _mask;

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
            _defaultState = defaultState;
            _mask = (1 << ordinal());
        }

        public boolean enabledByDefault() {
            return _defaultState;
        }

        // @since 2.3
        public boolean enabledIn(int flags) {
            return (flags & _mask) != 0;
        }

        public int getMask() {
            return _mask;
        }
    }
}
