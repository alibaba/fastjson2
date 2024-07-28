package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSONObject;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

public class Issue2762 {
    @Test
    public void test() throws JSONException {
        String jsonString = "{\"this0\":{\"$ref\":\"1\"}}";
        JSONObject jsonObject = JSONObject.parseObject(jsonString, JSONObject.class);
        JSONObject innerObject1 = (JSONObject) jsonObject.get("this0");
    }
}
