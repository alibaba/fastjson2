package com.alibaba.fastjson2.issues_3300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3382 {
    @Test
    public void test() throws Exception {
        String str = "2.503705945562304E-5";
        BigDecimal expected = new BigDecimal(str).stripTrailingZeros();

        BigDecimal decimal = (BigDecimal) JSON.parse(str, JSONReader.Feature.UseBigDecimalForDoubles);
        assertEquals(expected, decimal);
    }
}
