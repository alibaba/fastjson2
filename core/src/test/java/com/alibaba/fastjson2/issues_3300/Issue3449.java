package com.alibaba.fastjson2.issues_3300;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3449 {
    @Test
    public void test() {
        assertEquals("null", JSON.toJSONString(Double.NaN));
        assertEquals("null", new String(JSON.toJSONBytes(Double.NaN)));
    }
}
