package com.alibaba.fastjson2.function.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;

import java.util.List;
import java.util.function.Function;

public class ToDouble
        implements Function {
    final Double defaultValue;

    public ToDouble(Double defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public Object apply(Object o) {
        if (o == null) {
            return defaultValue;
        }

        if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue() ? 1D : 0D;
        }

        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }

        if (o instanceof String) {
            String str = (String) o;
            if (str.isEmpty()) {
                return defaultValue;
            }

            return Double.parseDouble(str);
        }

        if (o instanceof List) {
            List list = (List) o;
            JSONArray array = new JSONArray(list.size());
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                array.add(apply(item));
            }
            return array;
        }

        throw new JSONException("can not cast to Double " + o.getClass());
    }
}
