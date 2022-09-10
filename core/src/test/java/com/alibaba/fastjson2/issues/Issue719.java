package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue719 {
    @Test
    public void test() {
        assertEquals("{}", JSON.toJSONString(JSONObject.of(), JSONFactory.createWriteContext()));
        assertEquals("{}", JSON.toJSONString(JSONObject.of(), (JSONWriter.Context) null));
        assertEquals(
                "{}",
                JSONB.toJSONString(
                        JSONB.toBytes(JSONObject.of(), JSONFactory.createWriteContext())
                )
        );
        assertEquals(
                "{}",
                JSONB.toJSONString(
                        JSONB.toBytes(JSONObject.of(), (JSONWriter.Context) null)
                )
        );
    }
}
