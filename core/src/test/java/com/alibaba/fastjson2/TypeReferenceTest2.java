package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.BeanUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeReferenceTest2 {
    @Test
    public void test0() {
        BeanUtils.GenericArrayTypeImpl type = new BeanUtils.GenericArrayTypeImpl(int[].class);
        Bean bean = parse("{\"value\":[[1],[2]]}", type);
        Object[] value = (Object[]) bean.value;
        assertEquals(2, value.length);
        assertEquals(1, ((int[]) value[0])[0]);
        assertEquals(2, ((int[]) value[1])[0]);
    }

    @Test
    public void test1() {
        BeanUtils.GenericArrayTypeImpl type = new BeanUtils.GenericArrayTypeImpl(short[].class);
        Bean bean = parse("{\"value\":[[1],[2]]}", type);
        Object[] value = (Object[]) bean.value;
        assertEquals(2, value.length);
        assertEquals(1, ((short[]) value[0])[0]);
        assertEquals(2, ((short[]) value[1])[0]);
    }

    @Test
    public void test2() {
        BeanUtils.GenericArrayTypeImpl type = new BeanUtils.GenericArrayTypeImpl(byte[].class);
        Bean bean = parse("{\"value\":[[1],[2]]}", type);
        Object[] value = (Object[]) bean.value;
        assertEquals(2, value.length);
        assertEquals(1, ((byte[]) value[0])[0]);
        assertEquals(2, ((byte[]) value[1])[0]);
    }

    @Test
    public void test3() {
        BeanUtils.GenericArrayTypeImpl type = new BeanUtils.GenericArrayTypeImpl(long[].class);
        Bean bean = parse("{\"value\":[[1],[2]]}", type);
        Object[] value = (Object[]) bean.value;
        assertEquals(2, value.length);
        assertEquals(1, ((long[]) value[0])[0]);
        assertEquals(2, ((long[]) value[1])[0]);
    }

    @Test
    public void test4() {
        BeanUtils.GenericArrayTypeImpl type = new BeanUtils.GenericArrayTypeImpl(float[].class);
        Bean bean = parse("{\"value\":[[1],[2]]}", type);
        Object[] value = (Object[]) bean.value;
        assertEquals(2, value.length);
        assertEquals(1, ((float[]) value[0])[0]);
        assertEquals(2, ((float[]) value[1])[0]);
    }

    @Test
    public void test5() {
        BeanUtils.GenericArrayTypeImpl type = new BeanUtils.GenericArrayTypeImpl(double[].class);
        Bean bean = parse("{\"value\":[[1],[2]]}", type);
        Object[] value = (Object[]) bean.value;
        assertEquals(2, value.length);
        assertEquals(1, ((double[]) value[0])[0]);
        assertEquals(2, ((double[]) value[1])[0]);
    }

    @Test
    public void test6() {
        BeanUtils.GenericArrayTypeImpl type = new BeanUtils.GenericArrayTypeImpl(char[].class);
        Bean bean = parse("{\"value\":[[1],[2]]}", type);
        Object[] value = (Object[]) bean.value;
        assertEquals(2, value.length);
        assertEquals('1', ((char[]) value[0])[0]);
        assertEquals('2', ((char[]) value[1])[0]);
    }

    @Test
    public void test00() {
        BeanUtils.GenericArrayTypeImpl type = new BeanUtils.GenericArrayTypeImpl(int[][].class);
        Bean bean = parse("{\"value\":[[[1]],[[2]]]}", type);
        Object[] value = (Object[]) bean.value;
        assertEquals(2, value.length);
        assertEquals(1, ((int[]) ((Object[]) value[0])[0])[0]);
        assertEquals(2,  ((int[]) ((Object[]) value[1])[0])[0]);
    }

    public static <T> Bean<T> parse(String str, Type type) {
        return JSON.parseObject(str, new TypeReference<Bean<T>>(new Type[]{type}) {});
    }

    @Test
    public void test7() {
        BeanUtils.GenericArrayTypeImpl type = new BeanUtils.GenericArrayTypeImpl(boolean[].class);
        Bean bean = parse("{\"value\":[[false],[true]]}", type);
        Object[] value = (Object[]) bean.value;
        assertEquals(2, value.length);
        assertEquals(false, ((boolean[]) value[0])[0]);
        assertEquals(true, ((boolean[]) value[1])[0]);
    }

    public static class Bean<T> {
        public T value;
    }
}
