package com.alibaba.fastjson2.codec;

public class FieldInfo {
    public String fieldName;
    public String format;
    public int ordinal;
    public long features;
    public boolean ignore;
    public String[] alternateNames;
    public Class<?> writeUsing;

    public void init() {
        fieldName = null;
        format = null;
        features = 0;
        ignore = false;
        alternateNames = null;
        writeUsing = null;
    }
}
