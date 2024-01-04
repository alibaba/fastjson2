package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

public class Issue2138 {
    @Test
    public void test() {
        JSONObject jsonObject = JSONObject.of("name", "charmander", "age", 18, "birthday", "2000-01-01");
        byte[] jsonbBytes = jsonObject.toJSONBBytes();
        JSONPath path = JSONPath.of("$.name");
        path.extract(JSONReader.ofJSONB(jsonbBytes));
    }
}
