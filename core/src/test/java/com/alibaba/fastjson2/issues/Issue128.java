package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue128 {
    @Test
    public void test() {
        String str = "{\n" +
                "  \"code\": 1,\n" +
                "  \"message\": \"success\",\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"date\": \"2022-04-29T00:00:00+08:00\",\n" +
                "      \"value\": 1.01\n" +
                "    },\n" +
                "    {\n" +
                "      \"date\": \"2022-04-28T00:00:00+08:00\",\n" +
                "      \"value\": 1.02\n" +
                "    },\n" +
                "    {\n" +
                "      \"date\": \"2022-04-27T00:00:00+08:00\",\n" +
                "      \"value\": 1.03\n" +
                "    },\n" +
                "    {\n" +
                "      \"date\": \"2022-04-26T00:00:00+08:00\",\n" +
                "      \"value\": 1.03\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        JSONObject jsonObject = JSON.parseObject(str);
        Bean bean = jsonObject.toJavaObject(Bean.class);
        assertNotNull(bean);

        String data = jsonObject.getJSONArray("data").toString();
        JSON.parseArray(data, XXXXXXItem.class);
    }

    @Test
    public void test2() {
        String str = "[]";
    }

    public static class Bean {
    }

    public static class XXXXXXItem {
    }
}
