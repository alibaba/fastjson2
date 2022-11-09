package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue844 {
    @Test
    public void test() {
        JSONObject json1 = new JSONObject();
        json1.put(String.valueOf(1), 2);
        json1.put(String.valueOf(2), 3);
        json1.put(String.valueOf(3), 4);

        Map<Long, Long> status = JSONObject.parseObject(
                json1.toJSONString(),
                new TypeReference<Map<Long, Long>>() {}
        );
        for (Map.Entry<Long, Long> entry : status.entrySet()) {
            Object originValue = json1.getLongValue(String.valueOf(entry.getKey()));
            Long value = entry.getValue();
            assertEquals(originValue, value);
        }
    }
}
