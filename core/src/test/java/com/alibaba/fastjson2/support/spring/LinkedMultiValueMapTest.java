package com.alibaba.fastjson2.support.spring;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinkedMultiValueMapTest {
    @Test
    public void test() {
        JSONObject jsonObject = JSONObject.of(
                "values",
                JSONObject.of(
                        "100",
                        JSONArray.of(
                                JSONObject.of("id", 1001),
                                JSONObject.of("id", 1002)),
                        "200",
                        JSONArray.of(
                                JSONObject.of("id", 2001),
                                JSONObject.of("id", 2002))
                ));
        String str = jsonObject.toJSONString();
        System.out.println(str);
        Bean bean = JSON.parseObject(str, Bean.class);
        assertEquals(1001, bean.values.get("100").get(0).id);
        assertEquals(1002, bean.values.get("100").get(1).id);
        assertEquals(2001, bean.values.get("200").get(0).id);
        assertEquals(2002, bean.values.get("200").get(1).id);
    }

    public static class Bean {
        public LinkedMultiValueMap<String, InstanceMeta> values;
    }

    public static class InstanceMeta{
        public int id;
    }
}
