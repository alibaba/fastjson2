package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue972 {
    @Test
    public void test() {
        String str = JSON.toJSONString(Type.ACCEPTED);
        assertEquals("\"Accepted\"", str);
    }

    public enum Type {
        @JSONField(name = "Accepted")
        ACCEPTED,

        @JSONField(name = "Pending")
        PENDING,
    }

    @Test
    public void test0() {
        Bean bean = new Bean();
        bean.type = Type.PENDING;
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteEnumsUsingName);
        assertEquals("{\"type\":\"Pending\"}", str);
    }

    public static class Bean {
        public Type type;
    }
}
