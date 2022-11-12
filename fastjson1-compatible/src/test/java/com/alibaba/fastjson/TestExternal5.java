package com.alibaba.fastjson;

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

public class TestExternal5 {
    ParserConfig config = ParserConfig.global;

    @BeforeEach
    public void setUp() throws Exception {
        config.addAccept("com.alibaba.dubbo.demo");
    }

    @Test
    public void test_0() throws Exception {
        ExtClassLoader classLoader = new ExtClassLoader();
        Class<?> clazz = classLoader.loadClass("com.alibaba.dubbo.demo.MyEsbResultModel2");
        Method method = clazz.getMethod("setReturnValue", new Class[]{Serializable.class});

        Object obj = clazz.newInstance();
        method.invoke(obj, "AAAA");

        {
            String text = JSON.toJSONString(obj);
            assertEquals("{\"returnValue\":\"AAAA\",\"successed\":true}", text);
        }

        String text = JSON.toJSONString(obj, SerializerFeature.WriteClassName, SerializerFeature.WriteMapNullValue);
        Object object = JSON.parseObject(text, clazz, config);
        assertEquals(clazz.getName(), object.getClass().getName());

        Object object2 = JSON.parse(text, config);
        assertEquals(JSONObject.class, object2.getClass());
    }

    public static class ExtClassLoader
            extends ClassLoader {
        public ExtClassLoader() throws IOException {
            super(Thread.currentThread().getContextClassLoader());

            {
                byte[] bytes;
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external/MyEsbResultModel2.clazz");
                bytes = IOUtils.toByteArray(is);
                is.close();

                super.defineClass("com.alibaba.dubbo.demo.MyEsbResultModel2", bytes, 0, bytes.length);
            }
        }
    }
}
