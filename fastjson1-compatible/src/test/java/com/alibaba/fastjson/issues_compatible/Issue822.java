package com.alibaba.fastjson.issues_compatible;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue822 {
    @Test
    public void test() {
        Map<String, Long> map = new HashMap<>();
        map.put("100", 100L);
        String s = JSON.toJSONString(DemoString.builder().map(map).build());
        Demo demo = JSON.parseObject(s, Demo.class);
        assertNotNull(demo);
        assertNotNull(demo.getMap());
        assertEquals(100L, demo.getMap().get(100L));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Demo{
        private Map<Long, Long> map;
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DemoString{
        private Map<String, Long> map;
    }
}
