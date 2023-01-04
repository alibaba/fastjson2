package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;

/**
 * for graal vm support
 */
public class ProxyFactory {
    static volatile boolean METHOD_NEW_PROXY_INSTANCE_ERROR;
    static volatile MethodHandle METHOD_NEW_PROXY_INSTANCE;

    public static <T> T newProxyInstance(Class<T> objectClass, JSONObject object) {
        MethodHandle newProxyInstance = METHOD_NEW_PROXY_INSTANCE;
        try {
            if (newProxyInstance == null) {
                Class<?> proxyClass = Class.forName("java.lang.reflect.Proxy");
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(proxyClass);
                newProxyInstance = lookup.findStatic(
                        proxyClass,
                        "newProxyInstance",
                        MethodType.methodType(Object.class, ClassLoader.class, Class[].class, InvocationHandler.class)
                );
                METHOD_NEW_PROXY_INSTANCE = newProxyInstance;
            }
        } catch (Throwable ignored) {
            METHOD_NEW_PROXY_INSTANCE_ERROR = true;
        }

        try {
            return (T) newProxyInstance.invokeExact(objectClass.getClassLoader(), new Class[]{objectClass}, (InvocationHandler) object);
        } catch (Throwable e) {
            throw new JSONException("create proxy error : " + objectClass, e);
        }
    }
}
