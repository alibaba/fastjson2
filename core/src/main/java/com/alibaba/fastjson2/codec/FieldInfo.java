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
    public String defaultValue;

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
        defaultValue = null;
    }

   public static final long VALUE_MASK = 1L << 48;
   public static final long UNWRAPPED_MASK = 1L << 49;
}
