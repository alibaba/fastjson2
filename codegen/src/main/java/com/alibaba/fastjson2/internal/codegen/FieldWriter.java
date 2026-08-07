package com.alibaba.fastjson2.internal.codegen;

public class FieldWriter {
    public final int modifier;
    public final String name;
    public final Class fieldClass;

    public FieldWriter(int modifier, String name, Class fieldClass) {
        this.modifier = modifier;
        this.name = name;
        this.fieldClass = fieldClass;
    }
}
