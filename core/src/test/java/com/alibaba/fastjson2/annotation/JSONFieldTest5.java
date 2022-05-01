package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter.Feature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONFieldTest5 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 100;

        assertEquals("{\"id\":\"100\"}", JSON.toJSONString(bean));
    }

    public static class Bean {
        @JSONField(serializeFeatures = Feature.WriteNonStringValueAsString)
        public int id;
    }
}
