package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1469 {
    @Test
    public void test() {
        JSONObject posts = JSON.parseObject(STR);

        JSONPath jsonPath = JSONPath.of("$.posts[?(@.id == 1)]");
        jsonPath.setCallback(posts, e -> {
            JSONObject object = ((JSONObject) e).clone();
            object.put("title", "XXX");
            return object;
        });

        assertEquals(
                "{\"posts\":[{\"id\":1,\"title\":\"XXX\"},{\"id\":2,\"title\":\"DEF\"}]}",
                posts.toJSONString()
        );
    }

    static final String STR = "{\n" +
            "  \"posts\": [\n" +
            "    {\n" +
            "      \"id\": 1,\n" +
            "      \"title\": \"ABC\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 2,\n" +
            "      \"title\": \"DEF\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
}
