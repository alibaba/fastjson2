package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue369 {
    @Test
    public void test() {
        String str = "\"" + Issue369.class.getName() + "\"";
        Class<?> clazz = JSON.parseObject(str, Class.class, Feature.SupportClassForName);
        assertEquals(Issue369.class, clazz);

        assertThrows(JSONException.class, () -> JSON.parseObject(str, Class.class));
    }
}
