package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.JDKUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue994 {
    @Test
    public void test() throws Exception {
        if (JDKUtils.JVM_VERSION < 17) {
            return;
        }

        ExtClassLoader classLoader1 = new ExtClassLoader();
        Class objectClass = classLoader1.loadClass("com.alibaba.fastjson2.issue994.RiddleRandomResponse");
        String json = "{\n" +
                "                    \"code\": 200,\n" +
                "                    \"msg\": \"success\",\n" +
                "                    \"data\": {\n" +
                "                        \"title\": \"眼如铜铃，身象铁钉，有翅无毛，有脚难行。（猜一动物）\",\n" +
                "                        \"content\": \"眼如铜铃，身象铁钉，有翅无毛，有脚难行。（猜一动物）\",\n" +
                "                        \"answer\": \"蜻蜓\",\n" +
                "                        \"type\": \"ertongmiyu\",\n" +
                "                        \"time\": \"2020-04-10 17:51:49\",\n" +
                "                        \"type_name\": {\n" +
                "                            \"type\": \"ertongmiyu\",\n" +
                "                            \"name\": \"儿童谜语\",\n" +
                "                            \"desc\": \"儿童谜语是指适合儿童青少年的谜语，这类谜语一般和动物，植物等知识有关系，帮助儿童青少年丰富知识。儿童谜语一般不涉及成人内容。\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"time\": 1670310176,\n" +
                "                    \"log_id\": 458647883248488448\n" +
                "                }";

        Object object = JSON.parseObject(json, objectClass, JSONReader.Feature.SupportSmartMatch);
        String str = JSON.toJSONString(object);
        JSONObject jsonObject = JSON.parseObject(str);
        assertEquals(458647883248488448L, jsonObject.getLongValue("log_id"));
    }

    public static class ExtClassLoader
            extends ClassLoader {
        public ExtClassLoader() throws IOException {
            super(Thread.currentThread().getContextClassLoader());
            ClassLoader tcl = Thread.currentThread().getContextClassLoader();
            {
                byte[] bytes;
                InputStream is = tcl.getResourceAsStream("external/issue994/TypeNameDTO.clazz");
                bytes = IOUtils.toByteArray(is);
                is.close();

                super.defineClass("com.alibaba.fastjson2.issue994.TypeNameDTO", bytes, 0, bytes.length);
            }
            {
                byte[] bytes;
                InputStream is = tcl.getResourceAsStream("external/issue994/DataDTO.clazz");
                bytes = IOUtils.toByteArray(is);
                is.close();

                super.defineClass("com.alibaba.fastjson2.issue994.DataDTO", bytes, 0, bytes.length);
            }
            {
                byte[] bytes;
                InputStream is = tcl.getResourceAsStream("external/issue994/RiddleRandomResponse.clazz");
                bytes = IOUtils.toByteArray(is);
                is.close();

                super.defineClass("com.alibaba.fastjson2.issue994.RiddleRandomResponse", bytes, 0, bytes.length);
            }
        }
    }
}
