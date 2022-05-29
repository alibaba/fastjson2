package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSpecial_4 {
    @Test
    public void test_special() {
        String json = "{\"大小\":123}";
        JSONObject object = JSON.parseObject(json);
        Object obj = JSONPath.eval(object, "$.大小");
        assertEquals(123, obj);
    }
}
