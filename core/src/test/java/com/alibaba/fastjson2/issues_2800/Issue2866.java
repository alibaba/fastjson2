package com.alibaba.fastjson2.issues_2800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2866 {
    @Test
    public void test() throws Exception {
        String str = "0E-18";
        BigDecimal expected = new BigDecimal(str).stripTrailingZeros();

        BigDecimal decimal = (BigDecimal) JSON.parse(str, JSONReader.Feature.UseBigDecimalForDoubles);
        assertEquals(expected, decimal);
    }
}
