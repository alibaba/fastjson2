package com.alibaba.fastjson2.adapter.jackson.databind.util;

import com.alibaba.fastjson2.adapter.jackson.core.JsonGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPObjectTest {
    @Test
    public void test() throws Exception {
        JSONPObject jsonp = new JSONPObject("f", 123);
        assertEquals("f", jsonp.getFunction());
        assertEquals(123, jsonp.getValue());

        JsonGenerator gen = new JsonGenerator();
        jsonp.serializeWithType(gen, null, null);

        String str = gen.getJSONWriter().toString();
        assertEquals("f(123)", str);
    }
}
