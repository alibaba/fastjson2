package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CurrencyTest_2 {
    @Test
    public void test_0() throws Exception {
        VO vo = new VO();
        vo.setValue(Currency.getInstance(Locale.CHINA));
        vo.setValue1(Currency.getInstance(Locale.CHINA));
        String text = JSON.toJSONString(vo);
        VO vo1 = JSON.parseObject(text, VO.class);
        assertEquals(vo.value, vo1.value);
    }

    public static class VO {
        private Currency value;
        private Currency value1;

        public Currency getValue() {
            return value;
        }

        public void setValue(Currency value) {
            this.value = value;
        }

        public Currency getValue1() {
            return value1;
        }

        public void setValue1(Currency value1) {
            this.value1 = value1;
        }
    }
}
