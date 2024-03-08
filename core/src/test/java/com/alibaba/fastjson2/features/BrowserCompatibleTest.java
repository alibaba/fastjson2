package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrowserCompatibleTest {
    @Test
    public void test() {
        assertEquals(
                "9007199254740992",
                JSON.toJSONString(9007199254740992L));

        assertEquals(
                "\"9007199254740992\"",
                JSON.toJSONString(9007199254740992L, JSONWriter.Feature.BrowserCompatible));
    }
}
