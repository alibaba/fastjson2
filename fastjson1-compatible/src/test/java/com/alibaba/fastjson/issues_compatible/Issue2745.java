package com.alibaba.fastjson.issues_compatible;

import com.alibaba.fastjson.JSONArray;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2745 {
    @Test
    public void mutatedTest() throws Exception {
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(true);
        assertEquals(BigDecimal.ONE, jsonArray.getBigDecimal(0));
    }
}
