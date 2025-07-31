package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class JSONRefTest {
    @Test
    public void test() {
        JSONObject jsonObject = JSON.parseObject("{\"a \":{\"a\":123},\"b \":{\"$ref\":\"$.a\\\\ \"}}");
        assertSame(
                jsonObject.get("a "),
                jsonObject.get("b "));
    }
}
