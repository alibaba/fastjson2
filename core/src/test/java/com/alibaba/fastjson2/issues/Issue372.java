package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue372 {
    @Test
    public void test() {
        assertEquals(
                "{}",
                JSONObject
                        .of("a", null)
                        .toString(JSONWriter.Feature.NullAsDefaultValue)
        );
    }
}
