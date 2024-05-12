package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2477 {
    @Test
    public void jsonTest() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("startDate", LocalDate.of(2019,7, 14));
        map.put("endDate", LocalDate.of(2019,8, 14));
        assertEquals("{\"startDate\":\"2019-07-14\",\"endDate\":\"2019-08-14\"}", JSON.toJSONString(map));
    }
}
