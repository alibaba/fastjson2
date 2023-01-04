package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestExternal3 {
    @Test
    public void test_0() throws Exception {
        ExtClassLoader classLoader = new ExtClassLoader();
        Class<?> clazz = classLoader.loadClass("external.VO");
        Method method = clazz.getMethod("setName", new Class[]{String.class});
        Object obj = clazz.newInstance();
        method.invoke(obj, "jobs");

        Thread currentThread = Thread.currentThread();
        ClassLoader tcl = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(classLoader);

            String text = JSON.toJSONString(obj, SerializerFeature.WriteClassName);
            assertEquals("{\"@type\":\"external.VO\",\"id\":0,\"name\":\"jobs\"}", text);
            Object parse = JSON.parse(text, ParserConfig.global, Feature.SupportAutoType);
            String clazzName = parse.getClass().getName();
            assertEquals(clazz.getName(), clazzName);
        } finally {
            currentThread.setContextClassLoader(tcl);
        }
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
