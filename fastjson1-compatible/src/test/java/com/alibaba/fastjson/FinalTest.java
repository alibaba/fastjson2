package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FinalTest {
    @Test
    public void test_final() throws Exception {
        VO vo = new VO();
        String text = JSON.toJSONString(vo);
        assertEquals("{\"value\":1001}", text);
        JSON.parseObject(text, VO.class);
        JSON.parseObject("{\"id\":1001,\"value\":1001}", VO.class);
    }

    public static class VO {
        public static final int id = 1001;
        public final int value = 1001;
    }
}
