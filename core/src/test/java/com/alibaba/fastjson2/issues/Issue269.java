package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue269 {
    @Test
    public void test() {
        String jsonStr = "{\"test_prr\":2.9900000000000002131628207280300557613372802734375}";
        JSONObject json = JSON.parseObject(jsonStr);
        BigDecimal val = json.getBigDecimal("test_prr");
        assertEquals("2.99", Double.toString(val.doubleValue()));

        assertEquals(
                "{\"test_prr\":2.9900000000000002131628207280300557613372802734375}",
                json.toJSONString()
        );
        assertEquals(
                "{\"test_prr\":2.9900000000000002131628207280300557613372802734375}",
                JSON.toJSONString(json)
        );
    }
}
