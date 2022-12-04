package com.alibaba.fastjson2.adapter.jackson.databind.jsontype;

public class NamedType {
    private Class c;
    private String name;

    public NamedType(Class c, String name) {
        this.c = c;
        this.name = name;
    }
}
