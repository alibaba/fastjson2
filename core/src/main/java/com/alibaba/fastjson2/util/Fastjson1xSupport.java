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
    static Function UNSAFE_OBJECT_CREATOR;
    static Supplier UNSAFE_OBJECT_WRITER;

    static {
        Class objectClass = null;
        try {
            objectClass = Class.forName("com.alibaba.fastjson.JSONObject");
        } catch (ClassNotFoundException ignored) {
        }
        CLASS_FASTJSON_OBJECT = objectClass;
    }

    public static Function createObjectSupplier(Class objectClass) {
        if (JDKUtils.UNSAFE_SUPPORT) {
            if (UNSAFE_OBJECT_CREATOR != null) {
                return UNSAFE_OBJECT_CREATOR;
            }
            return UNSAFE_OBJECT_CREATOR = new ObjectCreatorUF(objectClass);
        }

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

    static class ObjectCreatorUF implements Function {
        final Class objectClass;
        final Field map;
        final long mapOffset;

        ObjectCreatorUF(Class objectClass) {
            this.objectClass = objectClass;
            try {
                map = objectClass.getDeclaredField("map");
            } catch (NoSuchFieldException e) {
                throw new JSONException("field map not found", e);
            }
            mapOffset = UnsafeUtils.UNSAFE.objectFieldOffset(map);
        }

        @Override
        public Object apply(Object map) {
            if (map == null) {
                map = new HashMap<>();
            }

            Object object;
            try {
                object = UnsafeUtils.UNSAFE.allocateInstance(objectClass);
                UnsafeUtils.UNSAFE.putObject(object, mapOffset, (Map) map);
            } catch (InstantiationException e) {
                throw new JSONException("create " + objectClass.getName() + " error", e);
            }
            return object;
        }
    }
}
