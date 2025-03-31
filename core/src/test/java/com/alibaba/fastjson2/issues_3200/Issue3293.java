package com.alibaba.fastjson2.issues_3200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue3293 {
    public interface ValueEnum<V> {
        /**
         * 获取枚举的 value 值
         */
        V getValue();
    }

    public enum MemberType implements ValueEnum<Integer> {
        USER(1),
        ADMIN(2),
        AGENT(3);

        final Integer value;

        MemberType(Integer value) {
            this.value = value;
        }

        @Override
        public Integer getValue() {
            return value;
        }
    }

    @Test
    public void test() {
        final ValueFilter valueFilter = (object, name, value) -> {
            if (value instanceof Enum<?>) {
                if (value instanceof ValueEnum<?>) {
                    return ((ValueEnum<?>) value).getValue();
                }
                return ((Enum<?>) value).ordinal();
            }
            return value;
        };
        LocalDateTime localDateTime = LocalDateTime.of(2006, 1, 2, 15, 4, 5);
        Map<Object, Object> map = new HashMap<>(4);
        map.put(MemberType.USER, 1);
        map.put(MemberType.ADMIN, 2);
        map.put(123, "a");
        map.put("key", "value");
        map.put(localDateTime, false);
        Date date = Date.from(localDateTime.plusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        map.put(date, true);
        map.put(BigDecimal.TEN, 10);
        map.put(Collections.emptyList(), 3L);
        map.put(Collections.emptyMap(), "null");

        // {"USER":1,{}:"null",[]:3,"ADMIN":2,"2006-01-03 15:04:05":true,10:10,123:"a","2006-01-02 15:04:05":false,"key":"value"}
        String rawJsonStr = JSON.toJSONString(map);
        // System.out.println(rawJsonStr);
        assertTrue(rawJsonStr.contains("\"USER\""));
        assertTrue(rawJsonStr.contains("{}:"));
        assertTrue(rawJsonStr.contains("[]:"));
        assertTrue(rawJsonStr.contains("10:"));
        assertTrue(rawJsonStr.contains("\"2006-01-02 15:04:05\":"));

        // {"USER":1,"{}":"null","[]":3,"ADMIN":2,"2006-01-03 15:04:05":true,"10":10,"123":"a","2006-01-02 15:04:05":false,"key":"value"}
        String jsonStr1 = JSON.toJSONString(map, JSONWriter.Feature.WriteNonStringKeyAsString);
        String jsonStr2 = JSON.toJSONString(map, valueFilter, JSONWriter.Feature.BrowserCompatible);
        // System.out.println(jsonStr1);
        assertEquals(jsonStr1, jsonStr2);

        assertTrue(jsonStr1.contains("\"USER\""));
        assertTrue(jsonStr1.contains("\"[]\":"));
        assertTrue(jsonStr1.contains("\"{}\":"));
        assertTrue(jsonStr1.contains("\"10\":"));
        assertTrue(jsonStr1.contains("\"2006-01-02 15:04:05\":"));
    }
}
