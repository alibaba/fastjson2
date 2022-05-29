package com.alibaba.fastjson2.v1issues.issue_1100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
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

        String json = JSON.toJSONString(result, JSONWriter.Feature.PrettyFormat);
        assertEquals("{\n" +
                "\t\"host\":\"127.0.0.1\",\n" +
                "\t\"port\":3306,\n" +
                "\t\"user\":{\n" +
                "\t\t\"name\":\"jack\",\n" +
                "\t\t\"age\":20\n" +
                "\t},\n" +
                "\t\"admin\":{\n" +
                "\t\t\"name\":\"jack\",\n" +
                "\t\t\"age\":20\n" +
                "\t}\n" +
                "}", json);

        JSONObject jsonObject2 = JSON.parseObject(json);
        assertEquals(result, jsonObject2);
    }
}
