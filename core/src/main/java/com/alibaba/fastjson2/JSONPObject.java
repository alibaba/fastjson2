package com.alibaba.fastjson2;

import java.util.ArrayList;
import java.util.List;

public class JSONPObject {
    private String function;

    private final List<Object> parameters = new ArrayList<>();

    public JSONPObject() {
    }

    public JSONPObject(String function) {
        this.function = function;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public void addParameter(Object parameter) {
        this.parameters.add(parameter);
    }

    public String toString() {
        return JSON.toJSONString(this);
    }
}
