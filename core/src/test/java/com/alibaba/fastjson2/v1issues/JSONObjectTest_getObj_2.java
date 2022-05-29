package com.alibaba.fastjson2.v1issues;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JSONObjectTest_getObj_2 {
    @Test
    public void test_get_empty() {
        JSONObject obj = new JSONObject();
        obj.put("value", "");
        assertEquals("", obj.get("value"));
        assertNull(
                obj.getObject("value", Model.class)
        );
        assertNull(
                obj.getObject("value", getType())
        );
    }

    public static class Model {
    }

    public static <T> Type getType() {
        return new TypeReference<T[]>() {
        }.getType();
    }
}
