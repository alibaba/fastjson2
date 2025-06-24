package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2570 {
    @Test
    public void test() {
        String json = "{0:12,1:13,2:14,\"date\":\"2024-05-14\"}";
        Map<Object, Object> map = JSON.parseObject(json, Map.class);

        Integer date0 = (Integer) map.get(0);
        assertNotNull(date0);

        Integer date1 = (Integer) map.get(1);
        assertNotNull(date1);
    }
}
