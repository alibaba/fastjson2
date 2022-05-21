package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class Fastjson1xSupport {
    static final Class CLASS_FASTJSON_OBJECT;

    static {
        Class objectClass = null;
        try {
            objectClass = Class.forName("com.alibaba.fastjson.JSONObject");
        } catch (ClassNotFoundException ignored) {
        }
        CLASS_FASTJSON_OBJECT = objectClass;
    }

    public static Function createObjectSupplier(Class objectClass) {
        Constructor constructor;
        try {
            constructor = objectClass.getConstructor(Map.class);
        } catch (NoSuchMethodException e) {
            throw new JSONException("create JSONObject1 error");
        }

        return (Object arg) -> {
            try {
                return constructor.newInstance(arg);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new JSONException("create JSONObject1 error");
            }
        };
    }
}
