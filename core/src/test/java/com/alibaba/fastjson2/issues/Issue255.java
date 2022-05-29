package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class Issue255 {
    @Test
    public void test0() {
        JSONArray array = new JSONArray();
        array.add(new JSONObject() {
            {
                put("type", "text");
                put("data", new JSONObject() {
                    {
                        put("content", "hello111");
                    }});
            }
        });
        array.add(new JSONObject() {
            {
                put("type", "text");
                put("data", new JSONObject() {
                    {
                        put("content", "hello222");
                    }});
            }});

        // Exception in thread "main" com.alibaba.fastjson2.JSONException: can not convert from class java.lang.String to class java.lang.String
        System.out.println(
                array.toJavaList(TestBean.class)
        );

        // [Test.TestBean(type=text, data={content=hello111}), Test.TestBean(type=text, data={content=hello222})]
        System.out.println(
                JSON.parseArray(array.toJSONString(), TestBean.class)
        );
    }

    @Data
    public static class TestBean {
        public String type;
        public Map<String, String> data;
    }
}
