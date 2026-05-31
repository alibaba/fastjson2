package com.alibaba.fastjson2;

import com.alibaba.fastjson.util.TypeUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class Issue3906 {
    @Test
    public void test() throws NoSuchMethodException {
        assertDoesNotThrow(() -> TypeUtils.castToTimestamp(new java.util.Date()));
    }
}
