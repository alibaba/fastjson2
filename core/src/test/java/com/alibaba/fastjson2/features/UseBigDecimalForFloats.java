package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UseBigDecimalForFloats {
    @Test
    public void decimal_as_float() {
        String str = "{\"val\":1.2}";
        JSONObject object = JSON.parseObject(str, JSONReader.Feature.UseBigDecimalForFloats);
        Object val = object.get("val");
        assertNotNull(val);
        assertEquals(Float.class, val.getClass());
    }

    @Test
    public void decimal_as_float_utf8() {
        String str = "{\"val\":1.2}";
        JSONObject object = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), JSONReader.Feature.UseBigDecimalForFloats);
        Object val = object.get("val");
        assertNotNull(val);
        assertEquals(Float.class, val.getClass());
    }

    @Test
    public void decimal_as_double() {
        String str = "{\"val\":1.2}";
        JSONObject object = JSON.parseObject(str, JSONReader.Feature.UseBigDecimalForDoubles);
        Object val = object.get("val");
        assertNotNull(val);
        assertEquals(Double.class, val.getClass());
    }

    @Test
    public void decimal_as_double_utf8() {
        String str = "{\"val\":1.2}";
        JSONObject object = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), JSONReader.Feature.UseBigDecimalForDoubles);
        Object val = object.get("val");
        assertNotNull(val);
        assertEquals(Double.class, val.getClass());
    }
}
