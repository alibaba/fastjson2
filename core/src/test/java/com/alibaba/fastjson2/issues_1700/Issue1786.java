package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1786 {
    @Test
    public void test() {
        String json = "{\"c1\":{\"a\":1,\"a\":2}}";
        JSONObject jsonObject = JSON.parseObject(json, JSONReader.Feature.DuplicateKeyValueAsArray);
        assertEquals("[1,2]", ((JSONObject) jsonObject.get("c1")).get("a").toString());
    }
}
