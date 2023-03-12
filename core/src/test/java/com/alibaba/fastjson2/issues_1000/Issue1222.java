package com.alibaba.fastjson2.issues_1000;

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

        HashMap<String, String> users2 = Optional
                .ofNullable(text)
                .map(
                        t -> (HashMap<String, String>) JSONObject.parseObject(
                                t,
                                new TypeReference<HashMap<String, String>>() {}
                        )
                ).orElse(new HashMap<>());

        assertEquals("5", users2.get("businessLine"));
    }
}
