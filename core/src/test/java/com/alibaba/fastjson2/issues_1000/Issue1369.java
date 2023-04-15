package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1369 {
    @Test
    public void test() {
        String path = "$.A.B[?(@.C==null)]";
        JSONPath jsonPath = JSONPath.of(path);
        String json = "{\"A\": {\"B\": {\"C\": null, \"D\": 1}}} ";
        String expected = "{\"C\":null,\"D\":1}";

        assertEquals(
                expected,
                JSON.toJSONString(
                        jsonPath.eval(
                                JSON.parseObject(json)
                        ),
                        JSONWriter.Feature.WriteNulls
                )
        );

        assertEquals(
                expected,
                JSON.toJSONString(JSONPath.extract(json, path), JSONWriter.Feature.WriteNulls)
        );
    }
}
