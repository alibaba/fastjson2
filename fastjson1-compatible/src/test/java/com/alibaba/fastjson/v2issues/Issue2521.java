package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2521 {
    public enum Type {
        @JSONField(name = "Rejected")
        REJECTED,

        @JSONField(name = "Pending")
        PENDING,
    }

    public static class Bean {
        public Type type;
    }

    public enum Type1 {
        @com.alibaba.fastjson.annotation.JSONField(name = "Rejected")
        REJECTED,

        @com.alibaba.fastjson.annotation.JSONField(name = "Pending")
        PENDING,
    }

    public static class Bean1 {
        public Type1 type;
    }

    @Test
    public void testMutated() {
        String str = JSON.toJSONString(Type.REJECTED);
        assertEquals("\"Rejected\"", str);
    }

    @Test
    public void testMutatedfj() {
        String str = com.alibaba.fastjson.JSON.toJSONString(Type1.REJECTED);
        assertEquals("\"Rejected\"", str);
    }

    @Test
    public void testMutated0() {
        Bean bean = new Bean();
        bean.type = Type.REJECTED;
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteEnumsUsingName);
        assertEquals("{\"type\":\"Rejected\"}", str);
    }

    @Test
    public void testMutated0fj() {
        Bean1 bean1 = new Bean1();
        bean1.type = Type1.REJECTED;
        String str = com.alibaba.fastjson.JSON.toJSONString(bean1, SerializerFeature.WriteEnumUsingName);
        assertEquals("{\"type\":\"Rejected\"}", str);
    }
}
