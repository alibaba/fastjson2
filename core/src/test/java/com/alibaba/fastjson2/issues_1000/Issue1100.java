package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1100 {
    @Test
    public void test() {
        String str = "{\"a\":0,\"b\":1,\"b\":2,\"b\":3}";
        JSONObject jsonObject = JSON.parseObject(str, JSONReader.Feature.DuplicateKeyValueAsArray);
        assertEquals("[1,2,3]", jsonObject.get("b").toString());
    }
}
