package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeReferenceTest2 {
    @Test
    public void test() {
        Bean bean = parse("{\"value\":[1,2]}", int[].class);
        int[] value = (int[]) bean.value;
        assertEquals(2, value.length);
        assertEquals(1, value[0]);
        assertEquals(2, value[1]);
    }

    public static <T> Bean<T> parse(String str, Class<T> clazz) {
        return JSON.parseObject(str, new TypeReference<Bean<T>>(new Type[]{clazz}) {});
    }

    public static class Bean<T> {
        public T value;
    }
}
