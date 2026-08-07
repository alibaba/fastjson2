package com.alibaba.fastjson.issue_1100;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 01/04/2017.
 */
public class Issue1121 {
    @Test
    public void test_for_issue() throws Exception {
        JSONObject userObject = new JSONObject();
        userObject.put("name", "jack");
        userObject.put("age", 20);

        JSONObject result = new JSONObject();
        result.put("host", "127.0.0.1");
        result.put("port", 3306);
        result.put("user", userObject);
        result.put("admin", userObject);

        String json = JSON.toJSONString(result, SerializerFeature.PrettyFormat);
        String json1 = result.toString(SerializerFeature.PrettyFormat);
        assertEquals(json, json1);

        JSONObject jsonObject2 = JSON.parseObject(json);
        assertEquals(result.toString(), jsonObject2.toString());
        assertEquals(result.toJSONString(), jsonObject2.toString());
    }
}
