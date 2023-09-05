package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1831 {
    @Test
    public void test() {
        String str = "1.09600000";
        BigDecimal decimal = new BigDecimal(str);
        assertEquals(str, JSON.toJSONString(decimal));
        assertEquals(str, new String(JSON.toJSONBytes(decimal)));
    }
}
