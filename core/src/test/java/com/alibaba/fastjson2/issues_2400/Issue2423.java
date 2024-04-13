package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Issue2423 {
    @Test
    void test() {
        Bean bean = new Bean();

        String jsonStr = JSON.toJSONString(bean);
        Bean object = JSON.parseObject(jsonStr, Bean.class);
        assertTrue(object.getSet().isEmpty());
        assertTrue(object.getMap().isEmpty());
        assertEquals(bean, object);

        jsonStr = com.alibaba.fastjson2.JSON.toJSONString(bean);
        object = com.alibaba.fastjson2.JSON.parseObject(jsonStr, Bean.class);
        assertTrue(object.getSet().isEmpty());
        assertTrue(object.getMap().isEmpty());
        assertEquals(bean, object);
    }

    @Data
    static class Bean {
        public Bean() {
        }
        private EnumSet<TimeUnit> set = EnumSet.noneOf(TimeUnit.class);
        private EnumMap<TimeUnit, Integer> map = new EnumMap<>(TimeUnit.class);
    }
}
