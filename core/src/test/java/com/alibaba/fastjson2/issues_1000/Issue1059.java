package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue1059 {
    @Test
    public void testRemove() {
        JSONPath path = JSONPath.of("$.a.b");

        JSONObject object = JSON.parseObject("{\n" +
                "  \"a\":{\n" +
                "     \"b\":\"xxx\"\n" +
                "   }\n" +
                "}");
        path.remove(object);
        assertEquals("{\"a\":{}}", object.toJSONString(JSONWriter.Feature.WriteNulls));

        JSONObject object1 = JSON.parseObject("{\n" +
                "  \"a\":[\n" +
                "      {\n" +
                "       \"b\":\"xxx\"\n" +
                "     }\n" +
                "  ]\n" +
                "}");
        path.remove(object1);
        assertEquals("{\"a\":[{}]}", object1.toJSONString(JSONWriter.Feature.WriteNulls));

        Bean bean = JSON.parseObject("{\n" +
                "  \"a\":{\n" +
                "     \"b\":\"xxx\"\n" +
                "   }\n" +
                "}", Bean.class);
        assertEquals("xxx", bean.a.b);
        path.remove(bean);
        assertNull(bean.a.b);
        assertEquals("{\"a\":{}}", JSON.toJSONString(bean));

        Bean1 bean1 = JSON.parseObject("{\n" +
                "  \"a\":[\n" +
                "      {\n" +
                "       \"b\":\"xxx\"\n" +
                "     }\n" +
                "  ]\n" +
                "}", Bean1.class);
        path.remove(bean1);
        assertEquals("{\"a\":[{}]}", JSON.toJSONString(bean1));
    }

    @Test
    public void testSetNull() {
        JSONPath path = JSONPath.of("$.a.b");

        JSONObject object = JSON.parseObject("{\n" +
                "  \"a\":{\n" +
                "     \"b\":\"xxx\"\n" +
                "   }\n" +
                "}");
        path.set(object, null);
        assertEquals("{\"a\":{\"b\":null}}", object.toJSONString(JSONWriter.Feature.WriteNulls));

        JSONObject object1 = JSON.parseObject("{\n" +
                "  \"a\":[\n" +
                "      {\n" +
                "       \"b\":\"xxx\"\n" +
                "     }\n" +
                "  ]\n" +
                "}");
        path.set(object1, null);
        assertEquals("{\"a\":[{\"b\":null}]}", object1.toJSONString(JSONWriter.Feature.WriteNulls));

        Bean bean = JSON.parseObject("{\n" +
                "  \"a\":{\n" +
                "     \"b\":\"xxx\"\n" +
                "   }\n" +
                "}", Bean.class);
        assertEquals("xxx", bean.a.b);
        path.set(bean, null);
        assertNull(bean.a.b);
        assertEquals("{\"a\":{\"b\":null}}", JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls));

        Bean1 bean1 = JSON.parseObject("{\n" +
                "  \"a\":[\n" +
                "      {\n" +
                "       \"b\":\"xxx\"\n" +
                "     }\n" +
                "  ]\n" +
                "}", Bean1.class);
        path.set(bean1, null);
        assertEquals("{\"a\":[{\"b\":null}]}", JSON.toJSONString(bean1, JSONWriter.Feature.WriteNulls));
    }

    public static class Bean {
        public Item a;
    }

    public static class Item {
        public String b;
    }

    public static class Bean1 {
        public List<Item> a;
    }
}
