package com.alibaba.fastjson;

import java.util.ArrayList;
import java.util.List;

public class JSONPObject
        extends com.alibaba.fastjson2.JSONPObject {
    private final List<Object> parameters = new ArrayList<Object>();

    public JSONPObject() {
    }

    public JSONPObject(String function) {
        super(function);
    }
}
