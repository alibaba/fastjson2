package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.List;

public class Issue2476 {
    @Test
    public void test() {
        List array = (List) JSONPath.read("{\n" +
                " \"data\": [\n" +
                " {\n" +
                " \"id\": 7315635187420579130\n" +
                " },\n" +
                " {\n" +
                " \"id\": 7315635187420579130\n" +
                " }\n" +
                " ]\n" +
                "}", "data");
        for (Object item : array) {
            JSONObject jsonObject = (JSONObject) item;
            System.out.println(jsonObject.get("id"));
        }
    }
}
