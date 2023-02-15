package com.alibaba.fastjson2.adapter.jackson.databind.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacksonToStringTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 1001L;
        assertEquals("{\"id\":\"1001\"}", JSON.toJSONString(bean));
    }

    public static class Bean {
        @JsonSerialize(using = ToStringSerializer.class)
        public Long id;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.id = 1001L;
        assertEquals("{\"id\":\"1001\"}", JSON.toJSONString(bean));
    }

    public static class Bean1 {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNonStringValueAsString)
        public Long id;
    }
}
