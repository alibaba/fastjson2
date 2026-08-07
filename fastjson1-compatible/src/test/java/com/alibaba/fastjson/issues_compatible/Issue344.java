package com.alibaba.fastjson.issues_compatible;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue344 {
    @Test
    public void test() {
        assertEquals("{\n" +
                "\t\"id\":123\n" +
                "}", JSON.toJSONString(JSONObject.parseObject("{\"id\":123}"), true));
    }
}
