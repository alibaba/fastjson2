package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1954 {
    @Test
    public void test() {
        JSONObject obj0 = JSONObject.parseObject(STR);
        JSONObject obj1 = JSON.parseObject(STR.getBytes(StandardCharsets.UTF_8));
        JSONObject obj2 = JSON.parseObject(STR.toCharArray());
        assertEquals(obj1, obj0);
        assertEquals(obj2, obj0);
    }

    static final String STR = "{ /*\n" +
            "   * Created by jiemu\n" +
            "   * @Author: admin\n" +
            "   * @Date: 2023-10-26 17:10:23\n" +
            "   * @Desc \n" +
            "  */\n" +
            "    \"name\": \"EUXFD0OP9PHYUBWI9M08\",\n" +
            "    \"version\": 1000,\n" +
            "    \"parser_version\": 1,\n" +
            "    \"scene\": {\n" +
            "        \"signals\": {\n" +
            " //TODO\n" +
            "},\n" +
            "        \"fusions\":{\n" +
            " //TODO\n" +
            "},\n" +
            "        \"actuator\": {\n" +
            "            \"actions\":[ ]        }\n" +
            "    }\n" +
            "}";
}
