package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTimeUnit {
    @Test
    public void test_0() throws Exception {
        String text = JSON.toJSONString(TimeUnit.DAYS);
        assertEquals(TimeUnit.DAYS, JSON.parseObject(text, TimeUnit.class));
    }
}
