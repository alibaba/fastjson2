package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2_vo.LongValue1;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("regression")
public class Issue425 {
    @Test
    public void test() {
        String str = "{\"v0000\":+000.00}";
        assertEquals(0, JSON.parseObject(str, LongValue1.class).getV0000());
    }
}
