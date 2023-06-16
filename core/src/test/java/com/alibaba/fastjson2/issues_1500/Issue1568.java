package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Gabriel
 * @date 2023/6/16 10:53:48
 */
public class Issue1568 {
    static class Time {
        public Date d = new Date();
    }

    @Test
    public void test() {
        Time t = new Time();
        t.d.setTime(1686905364389L);
        byte[] bytes = JSONB.toBytes(t);
        assertEquals("{\n" +
                "\t\"d\":1686905364389\n" +
                "}", JSONB.toJSONString(bytes));
    }
}
