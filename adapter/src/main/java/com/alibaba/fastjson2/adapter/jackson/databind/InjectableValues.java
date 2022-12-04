package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONException;

public abstract class InjectableValues {
    public static class Std
            extends InjectableValues
            implements java.io.Serializable {
        public Std addValue(String key, Object value) {
            // TODO addValue
            throw new JSONException("TODO");
        }
    }
}
