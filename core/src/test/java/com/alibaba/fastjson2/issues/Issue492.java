package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue492 {
    @Test
    public void test() {
        String json = "{\"avaLimit\":1.1111111E7}";
        JSONObject jsonObject = JSON.parseObject(json);
        assertEquals(1.1111111E7, jsonObject.get("avaLimit"));
        assertEquals(new BigDecimal("1.1111111E7"), jsonObject.getBigDecimal("avaLimit"));

        Bean bean = JSON.parseObject(json, Bean.class);
        assertEquals(new BigDecimal("1.1111111E7"), bean.avaLimit);
    }

    @Test
    public void test1() {
        String[] values = new String[] {
                "1.1111111E7",
                "1.1111111E-7",
                "1.0E7",
                "1.0E-7",
                "1.01E-100",
                "1.01E100",
        };

        for (String value : values) {
            String json = "{\"avaLimit\":" + value + "}";
            JSONObject jsonObject = JSON.parseObject(json);
            assertEquals(Double.parseDouble(value), jsonObject.get("avaLimit"));
            assertEquals(new BigDecimal(value), jsonObject.getBigDecimal("avaLimit"));

            Bean bean = JSON.parseObject(json, Bean.class);
            assertEquals(new BigDecimal(value), bean.avaLimit);
        }
    }

    static class Bean {
        public BigDecimal avaLimit;
    }
}
