package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

@Tag("reader")
public class JSONRefTest {
    @Test
    public void test() {
        JSONObject jsonObject = JSON.parseObject("{\"a \":{\"a\":123},\"b \":{\"$ref\":\"$.a\\\\ \"}}");
        assertSame(
                jsonObject.get("a "),
                jsonObject.get("b "));
    }
}
