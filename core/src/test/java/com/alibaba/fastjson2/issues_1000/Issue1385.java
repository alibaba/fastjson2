package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue1385 {
    @Test
    public void test() {
        Map map = new HashMap<>();
        map.put("type", null);
        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteMapNullValue);
        assertEquals("{\n" +
                "\t\"type\":null\n" +
                "}", JSONB.toJSONString(bytes));
        Bean bean = JSONB.parseObject(bytes, Bean.class);
        assertNull(bean.type);
    }

    @Test
    public void test1() {
        Type type = null;
        byte[] bytes = JSONB.toBytes(type, JSONWriter.Feature.WriteMapNullValue);
        assertEquals("null", JSONB.toJSONString(bytes));
        assertNull(JSONB.parseObject(bytes, Type.class));
    }

    public static class Bean {
        public Type type;
    }

    public enum Type {
        Big,
        Small,
        Medium
    }
}
