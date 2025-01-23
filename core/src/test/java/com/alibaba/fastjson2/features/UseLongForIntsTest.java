package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UseLongForIntsTest {
    @Test
    public void test() {
        long[] values = {
                1,
                12,
                123,
                1234,
                12345,
                123456,
                1234567,
                12345678,
                123456789,
                1234567890,
                12345678901L,
                123456789012L,
                1234567890123L,
                12345678901234L,
                123456789012345L,
                1234567890123456L,
                12345678901234567L,
                123456789012345678L,
                1234567890123456789L,
                Long.MAX_VALUE
        };
        for (long value : values) {
            String str = JSONObject.of("id", value).toJSONString();
            JSONObject object = JSON.parseObject(str, JSONReader.Feature.UseLongForInts);
            assertEquals(value, object.get("id"));
        }

        for (long value : values) {
            String str = JSONObject.of("id", -value).toJSONString();
            JSONObject object = JSON.parseObject(str, JSONReader.Feature.UseLongForInts);
            assertEquals(-value, object.get("id"));
        }

        for (long value : values) {
            String str = JSON.toJSONString(value);
            Long expected = value;

            Number number = JSON.parseObject(str, Number.class, JSONReader.Feature.UseLongForInts);
            assertEquals(expected, number);

            Object object = JSON.parseObject(str, Object.class, JSONReader.Feature.UseLongForInts);
            assertEquals(expected, object);
        }
    }

    @Test
    public void test1() {
        BigInteger value = new BigInteger("12345678901234567890");
        String str = JSONObject.of("id", value).toJSONString();
        JSONObject object = JSON.parseObject(str, JSONReader.Feature.UseLongForInts);
        assertEquals(value.longValue(), object.get("id"));
    }
}
