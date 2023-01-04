package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static com.alibaba.fastjson2.util.JDKUtils.JVM_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecordTest {
    static String json = "{\"@type\":\"com.alibaba.mock.demo.api.Demo\"}";

    @Test
    public void test0() throws Exception {
        if (JVM_VERSION < 14) {
            return;
        }

        ExtClassLoader classLoader1 = new ExtClassLoader();
        Class objectClass1 = classLoader1.loadClass("com.alibaba.fastjson2.test.LogoutEntity");

        Object object0 = JSON.parseObject("{\"serviceTicket\":\"aa\",\"url\":\"111\"}", objectClass1);
        Object object1 = JSON.parseObject("{\"serviceTicket\":\"bbb\",\"url\":\"222\"}", objectClass1);

        JSONObject jo = new JSONObject();
        jo.put("type", 2);
        jo.put("id", "cxcc");
        jo.put("uid", "uid");
        jo.put("data", JSONArray.of(object0, object1));
        assertEquals(
                "{\"type\":2,\"id\":\"cxcc\",\"uid\":\"uid\",\"data\":[{\"serviceTicket\":\"aa\",\"url\":\"111\"},{\"serviceTicket\":\"bbb\",\"url\":\"222\"}]}",
                jo.toJSONString()
        );
    }

    public static class ExtClassLoader
            extends ClassLoader {
        public ExtClassLoader() throws IOException {
            super(Thread.currentThread().getContextClassLoader());

            {
                byte[] bytes;
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external/LogoutEntity.clazz");
                bytes = IOUtils.toByteArray(is);
                is.close();

                super.defineClass("com.alibaba.fastjson2.test.LogoutEntity", bytes, 0, bytes.length);
            }
        }

        public InputStream getResourceAsStream(String name) {
            if ("com/alibaba/fastjson2/test/LogoutEntity.class".equals(name)) {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream("external/LogoutEntity.clazz");
            }
            return super.getResourceAsStream(name);
        }
    }
}
