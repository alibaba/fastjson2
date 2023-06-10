package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestExternal {
    @Test
    public void test_0() throws Exception {
        ExtClassLoader classLoader = new ExtClassLoader();
        Class<?> clazz = classLoader.loadClass("external.VO");
        Method method = clazz.getMethod("setName", new Class[]{String.class});
        Object obj = clazz.newInstance();
        method.invoke(obj, "jobs");

        String text = JSON.toJSONString(obj);
        Object object = JSON.parseObject(text, clazz);
        assertNotNull(object);
    }

    @Test
    public void test_1() throws Exception {
        ExtClassLoader classLoader = new ExtClassLoader();

        Class<?> clazz = classLoader.loadClass("external.VO");
        Method method = clazz.getMethod("setName", new Class[]{String.class});
        Object obj = clazz.newInstance();
        method.invoke(obj, "jobs");

        String text = JSON.toJSONString(obj);
        System.out.println(text);
        JSON.parseObject(text, clazz);
    }

    public static class ExtClassLoader
            extends ClassLoader {
        public ExtClassLoader() throws IOException {
            super(Thread.currentThread().getContextClassLoader());

            byte[] bytes;
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external/VO.clazz");
            bytes = IOUtils.toByteArray(is);
            is.close();

            super.defineClass("external.VO", bytes, 0, bytes.length);
        }
    }
}
