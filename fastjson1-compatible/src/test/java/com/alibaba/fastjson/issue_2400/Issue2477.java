package com.alibaba.fastjson.issue_2400;

import com.alibaba.fastjson.JSON;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class Issue2477 {

    @Test
    public void test_for_issue() {
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", LocalDate.now());
        map.put("endDate", LocalDate.now().plusMonths(1));
        JSONAssert.assertEquals("{\"endDate\":\"2024-05-28\",\"startDate\":\"2024-04-28\"}", JSON.toJSONString(map), true);
    }
}
