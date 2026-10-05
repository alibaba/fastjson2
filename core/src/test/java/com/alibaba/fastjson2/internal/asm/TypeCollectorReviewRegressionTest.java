package com.alibaba.fastjson2.internal.asm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TypeCollectorReviewRegressionTest {
    @Test
    public void zeroParameterMethodHasNoNames() {
        TypeCollector collector = new TypeCollector("test", new Class<?>[0]);
        collector.visitMethod(Opcodes.ACC_PUBLIC, "test", "()V");
        assertArrayEquals(new String[0], collector.getParameterNamesForMethod());
    }
}
