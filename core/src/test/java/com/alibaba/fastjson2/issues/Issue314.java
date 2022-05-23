package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteEnumsUsingName;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue314 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.type = Type.Big;
        assertEquals("{\"type\":\"Big\"}", JSON.toJSONString(bean));
        assertEquals("{\n" +
                "\t\"type\":\"Big\"\n" +
                "}", JSONB.toJSONString(JSONB.toBytes(bean)));
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.type = Type.Big;
        assertEquals("{\"type\":\"Big\"}", JSON.toJSONString(bean));
        assertEquals("{\n" +
                "\t\"type\":\"Big\"\n" +
                "}", JSONB.toJSONString(JSONB.toBytes(bean)));
    }

    public static class Bean {
        @JSONField(serializeFeatures = WriteEnumsUsingName)
        public Type type;
    }

    public static enum Type {
        Big, Small
    }

    @JSONType(serializeFeatures = WriteEnumsUsingName)
    public static class Bean1 {
        public Type type;
    }
}
