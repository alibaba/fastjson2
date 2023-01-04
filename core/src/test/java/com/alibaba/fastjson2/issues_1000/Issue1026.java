package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1026 {
    @Test
    public void test() {
        Map<String, Object> map = new LinkedHashMap<>(20);
        map.put("1", LocalDateTime.now());
        map.put("2", LocalDate.now());
        map.put("3", LocalTime.now());
        map.put("4", ZonedDateTime.now());
        map.put("5", new Date());
        map.put("6", Instant.now());
        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName);

        LinkedHashMap map1 = (LinkedHashMap) JSONB.parseObject(bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.SupportClassForName
        );
        assertEquals(map, map1);
    }
}
