package com.alibaba.fastjson2.internal.processor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodeGenUtilsTest {
    @Test
    public void getSupperClass() {
        for (int i = 1; i <= 12; i++) {
            assertEquals("ObjectReader" + i, CodeGenUtils.getSupperClass(i).getSimpleName());
        }
        assertEquals("ObjectReaderAdapter", CodeGenUtils.getSupperClass(13).getSimpleName());
    }

    @Test
    public void fieldReader() {
        for (int i = 1; i <= 10000; i++) {
            assertEquals("fieldReader" + i, CodeGenUtils.fieldReader(i));
        }
    }

    @Test
    public void fieldObjectReader() {
        for (int i = 1; i <= 10000; i++) {
            assertEquals("objectReader" + i, CodeGenUtils.fieldObjectReader(i));
        }
    }

    @Test
    public void fieldItemObjectReader() {
        for (int i = 1; i <= 10000; i++) {
            assertEquals("itemReader" + i, CodeGenUtils.fieldItemObjectReader(i));
        }
    }
}
