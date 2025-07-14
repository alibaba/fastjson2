package com.alibaba.fastjson2.issues_3500;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteBigDecimalAsPlain;

public class Issue3595 {
    @Test
    public void test() {
        BigDecimal dec = BigDecimal.valueOf(12345678, 3);
        JSON.toJSONString(dec, WriteBigDecimalAsPlain);
    }
}
