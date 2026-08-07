package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.clickhouse.data.value.UnsignedLong;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2958 {
    @Test
    public void test() throws Exception {
        String str = "9223372036854775808";
        UnsignedLong unsignedLong = UnsignedLong.valueOf(new BigInteger(str));
        String json = JSON.toJSONString(unsignedLong);
        assertEquals(str, json);
    }
}
