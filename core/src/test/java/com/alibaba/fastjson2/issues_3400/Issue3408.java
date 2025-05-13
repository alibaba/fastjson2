package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONReader;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3408 {
    @Test
    public void test() {
        String str = "{\"data\":[{\"username\":\"mike\"}, {\"user_name\":\"john\"}]}";

        JSONArray jsonArray = JSON.parseObject(str).getJSONArray("data");
        List<User> data1 = com.alibaba.fastjson.JSONObject.parseArray(JSON.toJSONString(jsonArray), User.class);
        List<User> data2 = JSON.parseObject(str).getJSONArray("data").toJavaList(User.class, JSONReader.Feature.SupportSmartMatch);

        assertEquals(JSON.toJSONString(data1), JSON.toJSONString(data2));
    }

    @Getter
    @Setter
    public static class User {
        String userName;
    }
}
