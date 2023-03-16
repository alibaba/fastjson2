package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1159 {
    @Test
    public void test() {
        JSONObject jsonObject = JSON.parseObject(s);
        String key = "data[*].title";

        Collection eval = (Collection) JSONPath.of(key, Collection.class).eval(jsonObject);
        assertEquals("[\"title_1\",\"title_3\"]", eval.toString());
    }

    @Test
    public void test1() {
        JSONObject jsonObject = JSON.parseObject(s);
        String key = "data[*].title";

        JSONPath jsonPath = JSONPath.of(key, Collection.class, JSONPath.Feature.KeepNullValue);
        Collection eval = (Collection) jsonPath.eval(jsonObject);
        assertEquals("[\"title_1\",null,\"title_3\"]", eval.toString());
    }

    static final String s = "{\n"
            + "    \"data\": [\n"
            + "        {\n"
            + "            \"title\": \"title_1\"\n"
            + "        },\n"
            + "        {\n"
            + "            \"title\": null\n"
            + "        },\n"
            + "        {\n"
            + "            \"title\": \"title_3\"\n"
            + "        }\n"
            + "    ]\n"
            + "}";
}
