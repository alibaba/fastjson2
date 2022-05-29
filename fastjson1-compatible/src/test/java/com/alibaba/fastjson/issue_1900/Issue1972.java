package com.alibaba.fastjson.issue_1900;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1972 {
    @Test
    public void test_for_issue() {
        JSONObject jsonObject = new JSONObject();
        final JSONObject a = new JSONObject();
        final JSONObject b = new JSONObject();
        a.put("b", b);

        b.put("c", "2018-04");
        b.put("d", new JSONArray());

        Integer obj = Integer.valueOf(123);

        jsonObject.put("a", a);
        JSONPath.arrayAdd(jsonObject, "$.a.b[?(@.c = '2018-04')].d", obj);

        assertEquals("{\"a\":{\"b\":{\"c\":\"2018-04\",\"d\":[123]}}}", jsonObject.toString());
    }
}
