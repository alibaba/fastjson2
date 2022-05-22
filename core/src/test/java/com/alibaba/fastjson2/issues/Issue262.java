package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.schema.JSONSchema;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue262 {
    @Test
    public void test() {
        assertEquals("\"Object\"", JSON.toJSONString(JSONSchema.Type.Object, JSONWriter.Feature.WriteEnumsUsingName));
    }
}
