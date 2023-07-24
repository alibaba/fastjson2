package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1679 {
    @Test
    public void test() {
        Map<TimeUnit, String> map = new LinkedHashMap<>();
        map.put(TimeUnit.DAYS, "day");
        EnumMap<TimeUnit, String> enumMap = new EnumMap(map);

        byte[] jsonbBytes = JSONB.toBytes(enumMap, JSONWriter.Feature.WriteClassName);
        EnumMap enumMap1 = JSONB.parseObject(jsonbBytes, EnumMap.class, JSONReader.Feature.SupportAutoType);
        assertEquals(enumMap.get(TimeUnit.DAYS), enumMap1.get(TimeUnit.DAYS));
    }
}
