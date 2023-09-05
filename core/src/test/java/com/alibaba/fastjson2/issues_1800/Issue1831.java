package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1831 {
    @Test
    public void test() {
        String[] strings = new String[] {
                "1.6",
                "1.06",
                "1.66",
                "1.096",
                "1.696",
                "1.09600000",
                "1.096000001"
        };
        for (String str : strings) {
            BigDecimal decimal = new BigDecimal(str);
            assertEquals(str, JSON.toJSONString(decimal));
            assertEquals(str, new String(JSON.toJSONBytes(decimal)));
        }
    }
}
