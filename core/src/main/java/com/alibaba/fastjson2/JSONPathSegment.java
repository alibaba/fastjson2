package com.alibaba.fastjson2;

import java.util.function.BiFunction;

abstract class JSONPathSegment {
    public abstract void accept(JSONReader jsonReader, JSONPath.Context context);

    public abstract void eval(JSONPath.Context context);

    public boolean contains(JSONPath.Context context) {
        eval(context);
        return context.value != null;
    }

    public boolean remove(JSONPath.Context context) {
        throw new JSONException("UnsupportedOperation " + getClass());
    }

    public void set(JSONPath.Context context, Object value) {
        throw new JSONException("UnsupportedOperation " + getClass());
    }

    public void setCallback(JSONPath.Context context, BiFunction callback) {
        throw new JSONException("UnsupportedOperation " + getClass());
    }

    public void setInt(JSONPath.Context context, int value) {
        set(context, Integer.valueOf(value));
    }

    public void setLong(JSONPath.Context context, long value) {
        set(context, Long.valueOf(value));
    }
}
