package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue3214 {
    @Test
    public void test() {
        String json = "{\n"
                + "  \"k1\":{\n"
                + "    \"k2\":[\n"
                + "      {\n"
                + "        {} : {}\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "}";
        JSONObject jsonObject = JSON.parseObject(json);
        assertNotNull(jsonObject);
    }
}
