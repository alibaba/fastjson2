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

public class TestExternal6 {
    ParserConfig config = ParserConfig.global;

    @BeforeEach
    public void setUp() throws Exception {
        config.addAccept("org.mule.esb.model");
    }

    @Test
    public void test_0() throws Exception {
        ExtClassLoader classLoader = new ExtClassLoader();
        Class<?> clazz = classLoader.loadClass("org.mule.esb.model.tcc.result.EsbResultModel");
        Method[] methods = clazz.getMethods();
        Method method = clazz.getMethod("setReturnValue", new Class[]{Serializable.class});

        Object obj = clazz.newInstance();

        {
            String text = JSON.toJSONString(obj);
            assertEquals("{\"exceptionDesc\":\"unknow exception\",\"methodType\":\"NOT_TCC_STATUS\",\"successed\":true}", text);
        }

        String text = JSON.toJSONString(obj, SerializerFeature.WriteClassName, SerializerFeature.WriteMapNullValue);
        JSON.parseObject(text, clazz, config);
        assertEquals(JSONObject.class, JSON.parse(text, config).getClass());
    }

    public static class ExtClassLoader
            extends ClassLoader {
        public ExtClassLoader() throws IOException {
            super(Thread.currentThread().getContextClassLoader());

            {
                byte[] bytes;
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external/EsbResultModel.clazz");
                bytes = IOUtils.toByteArray(is);
                is.close();

                super.defineClass("org.mule.esb.model.tcc.result.EsbResultModel", bytes, 0, bytes.length);
            }
            {
                byte[] bytes;
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external/EsbListBean.clazz");
                bytes = IOUtils.toByteArray(is);
                is.close();

                super.defineClass("org.esb.crm.tools.EsbListBean", bytes, 0, bytes.length);
            }
            {
                byte[] bytes;
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external/EsbHashMapBean.clazz");
                bytes = IOUtils.toByteArray(is);
                is.close();

                super.defineClass("org.esb.crm.tools.EsbHashMapBean", bytes, 0, bytes.length);
            }
        }
    }
}
