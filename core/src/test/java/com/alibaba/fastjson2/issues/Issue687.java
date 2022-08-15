package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue687 {
    @Test
    public void serialize() {
        assertEquals(
                "\"abc\"",
                JSON.toJSONString(
                        new StringBuffer("abc")
                )
        );
        assertEquals(
                "\"abc\"",
                JSON.toJSONString(
                        new StringBuilder("abc")
                )
        );
    }

    @Test
    public void deserialize() {
        String jsonStr = "\"abc\"";
        String str = "abc";

        Class[] types = new Class[]{StringBuffer.class, StringBuilder.class};
        for (Class objectClass : types) {
            Object object = JSON.parseObject(jsonStr, objectClass);
            assertSame(objectClass, object.getClass());
            assertEquals(str, object.toString());
        }
    }
}
