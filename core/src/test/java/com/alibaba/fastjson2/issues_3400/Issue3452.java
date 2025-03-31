package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue3452 {
    @Test
    public void test_0() throws Exception {
        String json = "{\"extInfo\":{\"$ref\":\"$.otherInfo.extInfo\"},\"id\":1,\"otherInfo\":{\"extInfo\":{\"age\":23}}}";
        JSONObject objectV2 = (JSONObject) JSON.parseObject(json, Object.class);
        assertSame(objectV2.get("extInfo"), objectV2.getJSONObject("otherInfo").get("extInfo"));
    }
}
