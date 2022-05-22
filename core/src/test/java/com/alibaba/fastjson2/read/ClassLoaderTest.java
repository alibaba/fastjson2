package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassLoaderTest {
    static String json = "{\"@type\":\"com.alibaba.mock.demo.api.Demo\"}";

    @Test
    public void test0() throws Exception {
        ExtClassLoader classLoader1 = new ExtClassLoader();
        Class objectClass1 = classLoader1.loadClass("com.alibaba.mock.demo.api.Demo");

        ExtClassLoader classLoader2 = new ExtClassLoader();
        Class objectClass2 = classLoader2.loadClass("com.alibaba.mock.demo.api.Demo");

        {
            Thread.currentThread().setContextClassLoader(classLoader1);
            Object object1 = JSON.parseObject(json, Object.class, JSONReader.Feature.SupportAutoType);
            assertEquals(objectClass1, object1.getClass());
        }
        {
            Thread.currentThread().setContextClassLoader(classLoader1);
            Object object1 = JSON.parseObject(json, Object.class, JSONReader.Feature.SupportAutoType);
            assertEquals(objectClass1, object1.getClass());
        }

        {
            Thread.currentThread().setContextClassLoader(classLoader2);
            Object object2 = JSON.parseObject(json, Object.class, JSONReader.Feature.SupportAutoType);
            assertEquals(objectClass2, object2.getClass());
        }
        {
            Thread.currentThread().setContextClassLoader(classLoader2);
            Object object2 = JSON.parseObject(json, Object.class, JSONReader.Feature.SupportAutoType);
            assertEquals(objectClass2, object2.getClass());
        }

        {
            Thread.currentThread().setContextClassLoader(classLoader1);
            Object object1 = JSON.parseObject(json, Object.class, JSONReader.Feature.SupportAutoType);
            assertEquals(objectClass1, object1.getClass());
        }
    }

    @Test
    public void test0_jsonb() throws Exception {
        ExtClassLoader classLoader1 = new ExtClassLoader();
        Class objectClass1 = classLoader1.loadClass("com.alibaba.mock.demo.api.Demo");

        ExtClassLoader classLoader2 = new ExtClassLoader();
        Class objectClass2 = classLoader2.loadClass("com.alibaba.mock.demo.api.Demo");

        byte[] jsonbBytes = JSONB.toBytes(objectClass1.newInstance(), JSONWriter.Feature.WriteClassName);

        {
            Thread.currentThread().setContextClassLoader(classLoader1);
            Object object1 = JSONB.parseObject(jsonbBytes, Object.class, JSONReader.Feature.SupportAutoType);
            assertEquals(objectClass1, object1.getClass());
        }
        {
            Thread.currentThread().setContextClassLoader(classLoader1);
            Object object1 = JSONB.parseObject(jsonbBytes, Object.class, JSONReader.Feature.SupportAutoType);
            assertEquals(objectClass1, object1.getClass());
        }

        {
            Thread.currentThread().setContextClassLoader(classLoader2);
            Object object2 = JSONB.parseObject(jsonbBytes, Object.class, JSONReader.Feature.SupportAutoType);
            assertEquals(objectClass2, object2.getClass());
        }
        {
            Thread.currentThread().setContextClassLoader(classLoader2);
            Object object2 = JSONB.parseObject(jsonbBytes, Object.class, JSONReader.Feature.SupportAutoType);
            assertEquals(objectClass2, object2.getClass());
        }

        {
            Thread.currentThread().setContextClassLoader(classLoader1);
            Object object1 = JSONB.parseObject(jsonbBytes, Object.class, JSONReader.Feature.SupportAutoType);
            assertEquals(objectClass1, object1.getClass());
        }
    }

    @Test
    public void test0_jsonb_1() throws Exception {
        ExtClassLoader classLoader1 = new ExtClassLoader();
        Class objectClass1 = classLoader1.loadClass("com.alibaba.mock.demo.api.Demo");

        ExtClassLoader classLoader2 = new ExtClassLoader();
        Class objectClass2 = classLoader2.loadClass("com.alibaba.mock.demo.api.Demo");

        byte[] jsonbBytes = JSONB.toBytes(objectClass1.newInstance(), JSONWriter.Feature.WriteClassName);

        {
            Thread.currentThread().setContextClassLoader(classLoader1);
            Object object1 = JSONB.parseObject(jsonbBytes, (Type) Object.class, JSONReader.Feature.SupportAutoType);
            assertEquals(objectClass1, object1.getClass());
        }
        {
            Thread.currentThread().setContextClassLoader(classLoader1);
            Object object1 = JSONB.parseObject(jsonbBytes, (Type) Object.class, JSONReader.Feature.SupportAutoType);
            assertEquals(objectClass1, object1.getClass());
        }

        {
            Thread.currentThread().setContextClassLoader(classLoader2);
            Object object2 = JSONB.parseObject(jsonbBytes, (Type) Object.class, JSONReader.Feature.SupportAutoType);
            assertEquals(objectClass2, object2.getClass());
        }
        {
            Thread.currentThread().setContextClassLoader(classLoader2);
            Object object2 = JSONB.parseObject(jsonbBytes, (Type) Object.class, JSONReader.Feature.SupportAutoType);
            assertEquals(objectClass2, object2.getClass());
        }

        {
            Thread.currentThread().setContextClassLoader(classLoader1);
            Object object1 = JSONB.parseObject(jsonbBytes, (Type) Object.class, JSONReader.Feature.SupportAutoType);
            assertEquals(objectClass1, object1.getClass());
        }
    }

    public static class ExtClassLoader
            extends ClassLoader {
        public ExtClassLoader() throws IOException {
            super(Thread.currentThread().getContextClassLoader());

            {
                byte[] bytes;
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external/Demo.clazz");
                bytes = IOUtils.toByteArray(is);
                is.close();

                super.defineClass("com.alibaba.mock.demo.api.Demo", bytes, 0, bytes.length);
            }
            {
                byte[] bytes;
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external/MockDemoService.clazz");
                bytes = IOUtils.toByteArray(is);
                is.close();

                super.defineClass("com.alibaba.mock.demo.service.MockDemoService", bytes, 0, bytes.length);
            }
        }
    }
}
