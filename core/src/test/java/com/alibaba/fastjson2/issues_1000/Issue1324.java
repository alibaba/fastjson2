package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1324 {
    public enum TestEnum {
        x1,
        x2
    }

    @Data
    public class Bean {
        Map<TestEnum, Integer> map;
    }

    @Test
    public void test() {
        String json = "{\"map\": {\"x1\": 1}}";
        Bean bean = JSON.parseObject(json, Bean.class);
        assertEquals(1, bean.map.size());
        assertEquals(1, bean.map.get(TestEnum.x1));
    }

    @Test
    public void test1() {
        String json = "{\"map\": {\"x1\": 1}}";
        Bean bean = JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportAutoType);
        assertEquals(1, bean.map.size());
        assertEquals(1, bean.map.get(TestEnum.x1));
    }
}
