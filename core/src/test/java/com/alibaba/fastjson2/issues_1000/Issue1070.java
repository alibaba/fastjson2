package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1070 {
    @Test
    public void testExtract() {
        String raw = "[[{\"a\": 1}]]";
        JSONArray arr = (JSONArray) JSONPath.extract(raw, "$[*][*]");
        assertEquals("[{\"a\":1}]", arr.toString());

        assertEquals(
                "[{\"a\":1}]",
                ((JSONArray) JSONPath.extract(raw, "$[0][*]")).toJSONString()
        );

        JSONArray array = JSON.parseArray(raw);
        assertEquals("[{\"a\":1}]", JSONPath.eval(array, "$[*][*]").toString());
        assertEquals("[{\"a\":1}]", JSONPath.eval(array, "$[0][*]").toString());
    }

    @Test
    public void testExtract1() {
        String raw = "[[{\"a\":1},{\"a\":2}],[{\"a\":3}]]";
        assertEquals("[{\"a\":3}]",
                ((JSONArray) JSONPath.extract(raw, "$[1][*]")).toJSONString());
        assertEquals("{\"a\":1}", ((JSONObject) JSONPath.extract(raw, "$[0][0]")).toJSONString());
        assertEquals("[1,2,3]", ((JSONArray) JSONPath.extract(raw, "$[*][*].a")).toJSONString());
    }

    @Test
    public void test_for_issue() throws Exception {
        String str = "[{\"id\":\"1\",\"name\":\"a\"},{\"id\":\"2\",\"name\":\"b\"}]";
        assertEquals("[\"1\",\"2\"]",
                JSONPath.extract(str, "$[*].id")
                        .toString()
        );
    }

    @Test
    public void test_for_issue_1() throws Exception {
        String str = "[{\"id\":\"1\",\"name\":\"a\"},{\"id\":\"2\",\"name\":\"b\"}]";
        assertEquals("[\"2\"]",
                JSONPath.extract(str, "$[?(@.name=='b')].id")
                        .toString()
        );
    }
}
