package com.alibaba.fastjson2.issues_3300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3375 {
    @Test
    public void test() {
        String json = "{\n" +
                "\t\"name\": \"\",\n" +
                "\t\"value\": [{\n" +
                "\t\t\"id\": \"101\",\n" +
                "\t\t\"additionalProperty\": [{\n" +
                "\t\t\t\t\"token\": \"token1\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"token\": \"token2\"\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t}]\n" +
                "}";
        Object object = JSON.parse(json);
        String path = "$.value.additionalProperty[*].token";
        JSONPath.set(object, path, "******");
        Object extract = JSONPath.extract(JSON.toJSONString(object), path);
        assertEquals("[\"******\",\"******\"]", JSON.toJSONString(extract));
    }
}
