package com.alibaba.fastjson2.codec;

public class FieldInfo {
    public String fieldName;
    public String format;
    public int ordinal;
    public long features;
    public boolean ignore;
    public String[] alternateNames;
    public Class<?> writeUsing;
    public boolean fieldClassMixIn;
    public boolean isTransient;
    public boolean isValue;

    public void init() {
        fieldName = null;
        format = null;
        ordinal = 0;
        features = 0;
        ignore = false;
        alternateNames = null;
        writeUsing = null;
        fieldClassMixIn = false;
        isTransient = false;
    }

   public static final long VALUE_MASK = 1L << 32;
}
