package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.reader.ObjectReader;

import java.lang.reflect.Constructor;
import java.util.Locale;

public class FieldInfo {
    public static final long VALUE_MASK = 1L << 48;
    public static final long UNWRAPPED_MASK = 1L << 49;
    public static final long RAW_VALUE_MASK = 1L << 50;
    public static final long READ_USING_MASK = 1L << 51;
    public static final long FIELD_MASK = 1L << 52;
    public static final long DISABLE_SMART_MATCH = 1L << 53;
    public static final long JIT = 1L << 54;
    public static final long DISABLE_UNSAFE = 1L << 55;
    public static final long READ_ONLY = 1L << 56;
    public static final long DISABLE_REFERENCE_DETECT = 1L << 57;
    public static final long DISABLE_ARRAY_MAPPING = 1L << 58;
    public static final long DISABLE_AUTO_TYPE = 1L << 59;
    public static final long DISABLE_JSONB = 1L << 60;
    public static final long BACKR_EFERENCE = 1L << 61;

    public String fieldName;
    public String format;
    public String label;
    public int ordinal;
    public long features;
    public boolean ignore;
    public String[] alternateNames;
    public Class<?> writeUsing;
    public Class<?> keyUsing;
    public Class<?> valueUsing;
    public Class<?> readUsing;
    public boolean fieldClassMixIn;
    public boolean isTransient;
    public String defaultValue;
    public Locale locale;
    public String schema;
    public boolean required;

    public ObjectReader getInitReader() {
        if (readUsing != null && ObjectReader.class.isAssignableFrom(readUsing)) {
            try {
                Constructor<?> constructor = readUsing.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (ObjectReader) constructor.newInstance();
            } catch (Exception ignored) {
                // ignored
            }
            return null;
        }
        return null;
    }

    public void init() {
        fieldName = null;
        format = null;
        label = null;
        ordinal = 0;
        features = 0;
        ignore = false;
        required = false;
        alternateNames = null;
        writeUsing = null;
        keyUsing = null;
        valueUsing = null;
        readUsing = null;
        fieldClassMixIn = false;
        isTransient = false;
        defaultValue = null;
        locale = null;
        schema = null;
    }
}
