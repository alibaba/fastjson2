package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1270 {
    @Test
    public void test() {
        String policy = "{\n" +
                "  \"Version\": \"1\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Action\": [\n" +
                "        \"log:PostLogStoreLogs\" \n" +
                "      ],\n" +
                "      \"Resource\": [\n" +
                "        //请务必完整填写到Logstore，否则会造成越权漏洞\n" +
                "        \"*\"\n" +
                "      ],\n" +
                "      \"Effect\": \"Allow\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        JSONObject object = JSON.parseObject(policy);
        assertEquals("1", object.get("Version"));
    }
}
