package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue642 {
    @Test
    public void test() {
        String str = "{\n" +
                "    \"x-limit\":10\n" +
                "}";
        JSONObject object = JSON.parseObject(str);
        assertEquals(
                10,
                JSONPath
                        .of("$.x\\-limit")
                        .eval(object)
        );
        assertEquals(
                10,
                JSONPath
                        .of("$['x-limit']")
                        .eval(object)
        );
    }
}
