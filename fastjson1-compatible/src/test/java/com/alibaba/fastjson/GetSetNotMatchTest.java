package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetSetNotMatchTest {
    @Test
    public void test_0() throws Exception {
        VO vo = new VO();
        vo.setValue(1);

        String text = JSON.toJSONString(vo);
        assertEquals("{\"value\":true}", text);
        VO vo1 = JSON.parseObject(text, VO.class);

        assertEquals(vo.getValue(), vo1.getValue());
    }

    public static class VO {
        private int value;
        public boolean getValue() {
            return value == 1;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
