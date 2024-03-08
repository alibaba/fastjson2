package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue2086 {
    @Test
    public void test() {
        Map<String, Object> map = new HashMap<>();
        map.put("mydate", new Date());
        map.put("wgh", 1);
        map.put("age", 21);
        RedisFastJsonWraper<Map> wraperSet = new RedisFastJsonWraper<>(map);
        String msg = JSON.toJSONString(wraperSet, SerializerFeature.WriteClassName);
        RedisFastJsonWraper wraper = JSONObject.parseObject(msg, RedisFastJsonWraper.class);
        assertTrue(((Map<String, Object>) wraper.value).get("mydate") instanceof Date);
    }

    public static class RedisFastJsonWraper<T> {
        private T value;

        public RedisFastJsonWraper() {
        }

        public RedisFastJsonWraper(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }
}
