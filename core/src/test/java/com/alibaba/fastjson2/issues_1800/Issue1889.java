package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1889 {
    @Test
    public void test() {
        String text = "{\n" +
                "  \"a\": {\n" +
                "    \"b\": 0,\n" +
                "    \"c\": 2\n" +
                "  }\n" +
                "}";
        JSONObject jsonObject = JSON.parseObject(text);
        JSONPath.remove(jsonObject, "$..b");
        assertEquals(
                "{\"a\":{\"c\":2}}",
                JSON.toJSONString(jsonObject, JSONWriter.Feature.WriteNulls));
    }
}
