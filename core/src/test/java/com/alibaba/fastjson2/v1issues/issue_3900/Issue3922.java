package com.alibaba.fastjson2.v1issues.issue_3900;

import com.alibaba.fastjson2.JSONObject;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class Issue3922 {
    @Test
    public void test_for_issue3922() throws JSONException {
        String jsonString = "{\"this0\":{\"$ref\":\"1\"}}";
        JSONObject jsonObject = JSONObject.parseObject(jsonString, JSONObject.class);
        JSONObject innerObject = (JSONObject) jsonObject.get("this0");
        JSONAssert.assertEquals("{\"$ref\":\"1\"}", innerObject.toString(), true);
    }
}
