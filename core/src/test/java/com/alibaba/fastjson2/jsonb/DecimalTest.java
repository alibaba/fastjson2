package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DecimalTest {
    @Test
    public void test() {
        BigDecimal[] decimals = new BigDecimal[] {
                BigDecimal.ONE,
                BigDecimal.ZERO,
                BigDecimal.TEN,
                new BigDecimal("2.3"),
                new BigDecimal("12.3"),
                new BigDecimal("12.34"),
                new BigDecimal("12.345"),
                new BigDecimal("12.3456"),
                BigDecimal.valueOf(Short.MAX_VALUE),
                BigDecimal.valueOf(Integer.MAX_VALUE),
                BigDecimal.valueOf(Long.MAX_VALUE)
        };

        for (int i = 0; i < decimals.length; i++) {
            byte[] bytes = JSONB.toBytes(decimals[i]);
            BigDecimal parsed = (BigDecimal) JSONB.parse(bytes);
            assertEquals(decimals[i], parsed);
        }
    }
}
