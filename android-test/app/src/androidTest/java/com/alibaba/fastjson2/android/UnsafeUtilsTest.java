package com.alibaba.fastjson2.android;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

public class UnsafeUtilsTest {
    @Test
    public void test() throws Throwable {
        Field[] fields = String.class.getDeclaredFields();
        System.out.printf("fields size " + fields.length);
        for (Field field : fields) {
            System.out.printf(field.getName());
        }
    }
}
