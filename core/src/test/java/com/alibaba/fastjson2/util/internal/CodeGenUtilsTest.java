package com.alibaba.fastjson2.util.internal;

import com.alibaba.fastjson2.internal.CodeGenUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodeGenUtilsTest {
    @Test
    public void test() {
        assertEquals("fieldReader123", CodeGenUtils.fieldReader(123));
    }
}
