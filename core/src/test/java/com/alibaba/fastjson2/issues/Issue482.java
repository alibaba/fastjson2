package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("regression")
public class Issue482 {
    @Test
    public void test() {
        assertNull(JSON.parseObject((String) null));
    }
}
