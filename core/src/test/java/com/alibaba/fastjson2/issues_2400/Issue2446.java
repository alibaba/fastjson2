package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.time.Period;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2446 {
    @Test
    public void test() {
        Period period = Period.ofDays(2);
        String str = JSON.toJSONString(period);
        assertEquals("\"P2D\"", str);
        assertEquals(period, JSON.parseObject(str, Period.class));
    }
}
