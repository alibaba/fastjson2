package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultiTypeTest {
    String str = "[{\"id\":123},{\"id\":234}]";
    byte[] jsonbBytes = JSON.parseArray(str).toJSONBBytes();

    @Test
    public void test() {
        Object[] array = JSON.parseObject(str, TypeReference.of(Bean1.class, Bean2.class));
        assertEquals(123, ((Bean1) array[0]).id);
        assertEquals(234, ((Bean2) array[1]).id);
    }

    @Test
    public void test1() {
        Object[] array = JSON.parseObject(str, Bean1.class, Bean2.class);
        assertEquals(123, ((Bean1) array[0]).id);
        assertEquals(234, ((Bean2) array[1]).id);
    }

    @Test
    public void test2() {
        Object[] array = JSONB.parseObject(jsonbBytes, TypeReference.of(Bean1.class, Bean2.class));
        assertEquals(123, ((Bean1) array[0]).id);
        assertEquals(234, ((Bean2) array[1]).id);
    }

    @Test
    public void test3() {
        Object[] array = JSONB.parseObject(jsonbBytes, Bean1.class, Bean2.class);
        assertEquals(123, ((Bean1) array[0]).id);
        assertEquals(234, ((Bean2) array[1]).id);
    }

    @Test
    public void testArray() {
        List array = JSON.parseArray(str, Bean1.class, Bean2.class);
        assertEquals(123, ((Bean1) array.get(0)).id);
        assertEquals(234, ((Bean2) array.get(1)).id);
    }

    @Test
    public void testArray1() {
        List array = JSON.parseArray(str, new Type[]{Bean1.class, Bean2.class}, JSONReader.Feature.ErrorOnNotSupportAutoType);
        assertEquals(123, ((Bean1) array.get(0)).id);
        assertEquals(234, ((Bean2) array.get(1)).id);
    }

    @Test
    public void testArray_jsonb() {
        List array = JSONB.parseArray(jsonbBytes, Bean1.class, Bean2.class);
        assertEquals(123, ((Bean1) array.get(0)).id);
        assertEquals(234, ((Bean2) array.get(1)).id);
    }

    @Test
    public void testArray_jsonb1() {
        List array = JSONB.parseArray(jsonbBytes, new Type[]{Bean1.class, Bean2.class}, JSONReader.Feature.ErrorOnNotSupportAutoType);
        assertEquals(123, ((Bean1) array.get(0)).id);
        assertEquals(234, ((Bean2) array.get(1)).id);
    }

    public static class Bean1 {
        public int id;
    }

    public static class Bean2 {
        public int id;
    }
}
