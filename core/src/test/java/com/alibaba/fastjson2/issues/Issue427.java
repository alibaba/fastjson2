package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue427 {
    @Test
    public void test() {
        String str = "{\n" +
                "\t\"salaryStart\": {\n" +
                "\t\t\"$numberDecimal\": \"6000\"\n" +
                "\t},\n" +
                "\t\"salaryEnd\": {\n" +
                "\t\t\"$numberDecimal\": \"9000\"\n" +
                "\t}\n" +
                "}";
        Bean bean = JSON.parseObject(str, Bean.class);
        assertEquals(new BigDecimal("6000"), bean.salaryStart);
        assertEquals(new BigDecimal("9000"), bean.salaryEnd);

        Bean bean1 = JSON.parseObject(str).to(Bean.class);
        assertEquals(new BigDecimal("6000"), bean1.salaryStart);
        assertEquals(new BigDecimal("9000"), bean1.salaryEnd);
    }

    public static class Bean {
        public BigDecimal salaryStart;
        public BigDecimal salaryEnd;
    }
}
