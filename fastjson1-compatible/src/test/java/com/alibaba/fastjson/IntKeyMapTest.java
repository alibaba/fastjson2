package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IntKeyMapTest {
    @Test
    public void test_0() throws Exception {
        JSONObject parse = (JSONObject) JSON.parse("{1:\"AA\",2:{}}");
        assertNotNull(parse.get(1));
    }
}
