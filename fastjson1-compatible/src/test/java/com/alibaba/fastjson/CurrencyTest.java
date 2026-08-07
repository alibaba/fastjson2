package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CurrencyTest {
    @Test
    public void test_0() throws Exception {
        VO vo = new VO();
        vo.setValue(Currency.getInstance(Locale.CHINA));
        String text = JSON.toJSONString(vo);
        VO vo1 = JSON.parseObject(text, VO.class);
        assertEquals(vo.value, vo1.value);
    }

    public static class VO {
        private Currency value;

        public Currency getValue() {
            return value;
        }

        public void setValue(Currency value) {
            this.value = value;
        }
    }
}
