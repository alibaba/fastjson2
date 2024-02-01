package com.alibaba.fastjson2_demo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class ContextDemo {
    JSONObject cachedObject = new JSONObject();
    JSONReader.Context readContext = JSONFactory.createReadContext(() -> cachedObject);

    @Test
    public void test() {
        JSONObject jsonObject = (JSONObject) JSON.parse("{\"id\":123}", readContext);
        assertSame(cachedObject, jsonObject);

        JSONObject jsonObject1 = (JSONObject) JSON.parse("{\"id\":234}", readContext);
        assertSame(cachedObject, jsonObject1);
    }
}
