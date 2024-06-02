package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2656 {
    @Test
    public void test() {
        JSONPath path = JSONPath.of("$.a.b.name");
        JSONObject jsonObject = new JSONObject();
        path.set(jsonObject, null);
        assertEquals("{\"a\":{\"b\":{\"name\":null}}}", jsonObject.toJSONString(JSONWriter.Feature.WriteNulls));
    }
}
