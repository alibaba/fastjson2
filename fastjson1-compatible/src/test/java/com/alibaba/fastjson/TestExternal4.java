package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestExternal4 {
    ParserConfig config = new ParserConfig();

    @BeforeEach
    public void setUp() throws Exception {
        config.addAccept("external.VO2");
        config.setAutoTypeSupport(true);
    }

    @Test
    public void test_0() throws Exception {
        ExtClassLoader classLoader = new ExtClassLoader();
        Class<?> clazz = classLoader.loadClass("external.VO2");
        Method method = clazz.getMethod("setName", new Class[]{String.class});
        Method methodSetValue = clazz.getMethod("setValue", new Class[]{Serializable.class});

        Object obj = clazz.newInstance();
        method.invoke(obj, "jobs");
        methodSetValue.invoke(obj, obj);

        {
            String text = JSON.toJSONString(obj);
            assertEquals("{\"id\":0,\"name\":\"jobs\",\"value\":{\"$ref\":\"..\"}}", text);
        }

        Thread currentThread = Thread.currentThread();
        ClassLoader tcl = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(classLoader);

            String text = JSON.toJSONString(obj, SerializerFeature.WriteClassName);
            assertEquals("{\"@type\":\"external.VO2\",\"id\":0,\"name\":\"jobs\",\"value\":{\"$ref\":\"..\"}}", text);
            Object o1 = JSON.parseObject(text, clazz, config);
            assertEquals(obj.getClass(), o1.getClass());
            Object o2 = JSON.parse(text, config, Feature.SupportAutoType);
            assertEquals(obj.getClass(), o2.getClass());
        } finally {
            currentThread.setContextClassLoader(tcl);
        }
    }

    public static class ExtClassLoader
            extends ClassLoader {
        public ExtClassLoader() throws IOException {
            super(Thread.currentThread().getContextClassLoader());

            {
                byte[] bytes;
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external/VO2.clazz");
                bytes = IOUtils.toByteArray(is);
                is.close();

                super.defineClass("external.VO2", bytes, 0, bytes.length);
            }
        }
    }
}
