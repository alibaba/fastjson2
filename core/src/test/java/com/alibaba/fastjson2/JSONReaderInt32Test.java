package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONReaderInt32Test {
    @Test
    public void decimalOverflow() {
        for (String value : new String[]{
                "10000000000",
                "10000000000.0",
                "10000000000.00",
                "1e10",
                "1E10",
                "1.0e10",
                "100e8",
                "0.1e11"
        }) {
            String json = "{\"count\":" + value + "}";
            assertThrows(JSONException.class, () -> JSON.parseObject(json, Bean.class), value);
            assertThrows(JSONException.class, () -> JSON.parseObject(json.toCharArray(), Bean.class), value);
            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(json.getBytes(StandardCharsets.UTF_8), Bean.class),
                    value
            );

            Bean bean = JSON.parseObject(json, Bean.class, JSONReader.Feature.NonErrorOnNumberOverflow);
            assertEquals(new BigDecimal(value).intValue(), bean.count, value);
        }
    }

    @Test
    public void decimalFractionWithinRange() {
        assertEquals(1, JSON.parseObject("{\"count\":1.9}", Bean.class).count);
        assertEquals(-1, JSON.parseObject("{\"count\":-1.9}", Bean.class).count);
        assertEquals(Integer.MAX_VALUE, JSON.parseObject("{\"count\":2147483647.9}", Bean.class).count);
        assertEquals(Integer.MIN_VALUE, JSON.parseObject("{\"count\":-2147483648.9}", Bean.class).count);
    }

    public static class Bean {
        public int count;
    }
}
