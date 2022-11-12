package com.alibaba.fastjson;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestExternal {
    @Test
    public void test_0() throws Exception {
        ExtClassLoader classLoader = new ExtClassLoader();
        Class<?> clazz = classLoader.loadClass("external.VO");
        Method method = clazz.getMethod("setName", new Class[]{String.class});
        Object obj = clazz.newInstance();
        method.invoke(obj, "jobs");

        String text = JSON.toJSONString(obj);
        assertEquals("{\"id\":0,\"name\":\"jobs\"}", text);
        Object obj1 = JSON.parseObject(text, clazz);
        assertEquals(text, JSON.toJSONString(obj1));
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
