package com.alibaba.fastjson2.adapter.jackson.databind.type;

import com.alibaba.fastjson2.adapter.jackson.core.type.TypeReference;
import com.alibaba.fastjson2.adapter.jackson.databind.JavaType;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TypeFactoryTest {
    @Test
    public void test() {
        JavaType javaType = TypeFactory
                .defaultInstance()
                .constructType(new TypeReference<SerializedValue<Object>>() {
                });
    }

    @Test
    public void test1() {
        com.fasterxml.jackson.databind.JavaType javaType = com.fasterxml.jackson.databind.type.TypeFactory
                .defaultInstance()
                .constructType(new com.fasterxml.jackson.core.type.TypeReference<SerializedValue<Object>>() {
                });
        System.out.println(javaType.getClass());
    }

    public static class SerializedValue<T> {
        private final T value;

        public SerializedValue(T value) throws IOException {
            this.value = value;
        }

        public T getValue() {
            return value;
        }
    }
}
