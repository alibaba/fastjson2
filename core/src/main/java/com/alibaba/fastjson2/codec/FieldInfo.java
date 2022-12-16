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
    public static final long JSON_AUTO_WIRED_ANNOTATED = 1L << 53;

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
