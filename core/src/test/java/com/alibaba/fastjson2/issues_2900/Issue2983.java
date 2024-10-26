package com.alibaba.fastjson2.issues_2900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2983 {
    @Test
    public void test() {
        String text = "{\n" +
                "    \"a\": {\n" +
                "            \"key\": 1    // test\n" +
                "    }\n" +
                "}";

        JSONObject jsonObject = JSON.parseObject(text);
        assertNotNull(jsonObject);
    }
}
