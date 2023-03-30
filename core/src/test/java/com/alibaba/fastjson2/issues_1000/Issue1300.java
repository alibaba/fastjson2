package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.TypeUtils;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1300 {
    @Test
    public void test() {
        BigDecimal decimal = new BigDecimal("123.45");
        Decimal128 decimal128 = new Decimal128(decimal);

        assertEquals("123.45", JSON.toJSONString(decimal128));

        assertEquals(
                decimal,
                TypeUtils.cast(decimal128, BigDecimal.class)
        );

        assertEquals(
                decimal,
                TypeUtils.toBigDecimal(decimal128)
        );
    }
}
