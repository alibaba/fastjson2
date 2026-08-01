package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("regression")
public class Issue493 {
    @Test
    public void test() {
        JSONObject jsonObject = JSONObject.parseObject("{\"time\":1655714717589}");
        assertEquals(1655714717589L, jsonObject.getDate("time").getTime());
    }
}
