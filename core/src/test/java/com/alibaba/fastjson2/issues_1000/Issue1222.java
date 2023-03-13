package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1222 {
    @Test
    public void test() {
        String text = "{\"businessLine\":5,\"ccId\":\"913440103290\",\"cardNo\":\"24000000039QWERR\"}";

        HashMap<String, String> users0 = Optional
                .ofNullable(text)
                .map(
                        t -> com.alibaba.fastjson.JSON.parseObject(
                                t,
                                new com.alibaba.fastjson.TypeReference<HashMap<String, String>>() {}
                        )
                ).orElse(new HashMap<>());

        HashMap<String, String> users1 = Optional
                .ofNullable(text)
                .map(
                        t -> JSON.parseObject(
                                t,
                                new TypeReference<HashMap<String, String>>() {}
                        )
                ).orElse(new HashMap<>());

        assertEquals("5", users1.get("businessLine"));

        HashMap<String, String> users2 = Optional
                .ofNullable(text)
                .map(
                        t -> JSONObject.parseObject(
                                t,
                                new TypeReference<HashMap<String, String>>() {}
                        )
                ).orElse(new HashMap<>());

        assertEquals("5", users2.get("businessLine"));
    }
}
