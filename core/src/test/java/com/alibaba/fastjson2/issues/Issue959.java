package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue959 {
    @Test
    public void test() {
        assertEquals(
                -2.0089457919266330204e-15,
                JSON.parseObject("{\"V\": -2.0089457919266330204e-15}").get("V")
        );
    }
}
