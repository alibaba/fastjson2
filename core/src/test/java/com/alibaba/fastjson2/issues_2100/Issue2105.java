package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2105 {
    @Test
    public void test() {
        String str = "{\n" +
                "\t\"uid\": \"123456789\",\n" +
                "\t\"clientSn\": \"A0001\",\n" +
                "\t\"model\": {\n" +
                "\t\t\"identity\": \"client\",\n" +
                "\t\t\"dataformat\": \"HEX\",\n" +
                "\t\t\"uid\": \"10086\",\n" +
                "\t\t\"protocol\": \"TCP\",\n" +
                "\t\t\"type\":\"CMD\",\n" +
                "\t}\n" +
                "}";
        Bean bean = JSON.parseObject(str, Bean.class);
        assertEquals("123456789", bean.uid);
        assertEquals("A0001", bean.clientSn);
        assertEquals("TCP", bean.model.protocol);
        assertEquals("CMD", bean.model.type);
    }

    public static class Bean {
        public String uid;
        public String clientSn;
        public Model model;
    }

    public static class Model {
        public String protocol;
        public String type;
    }
}
