package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue843 {
    @Test
    public void test() {
        JSONObject object = new JSONObject().fluentPut("1", 2).fluentPut("2", null).fluentPut("3", 4);
        assertEquals("{\"1\":2,\"3\":4}", object.toString());
        assertEquals("{\"1\":2,\"2\":null,\"3\":4}", object.toString(JSONWriter.Feature.WriteMapNullValue));
    }
}
