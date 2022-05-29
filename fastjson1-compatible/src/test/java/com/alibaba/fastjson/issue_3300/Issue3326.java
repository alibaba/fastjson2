package com.alibaba.fastjson.issue_3300;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3326 {
    @Test
    public void test_for_issue() throws Exception {
        HashMap<String, Number> map = JSON.parseObject("{\"id\":10.0}",
                new TypeReference<HashMap<String, Number>>() {
                }.getType()
        );
        assertEquals(BigDecimal.valueOf(10.0), map.get("id"));
    }
}
