package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeReferenceTest3 {
    @Test
    public void test() {
        ParameterizedTypeImpl type = new ParameterizedTypeImpl(Response.class, BigInteger.class);
        Response response = JSON.parseObject("{\"value\":123}", type);
        assertEquals(BigInteger.valueOf(123), response.value);
    }

    public static class Response<T> {
        public T value;
    }

    @Test
    public void test1() {
        ParameterizedTypeImpl type = new ParameterizedTypeImpl(Response1.class, BigInteger.class);
        Response1 response = JSON.parseObject("{\"value\":123}", type);
        assertEquals(BigInteger.valueOf(123), response.value);
    }

    public static class Response1<T> {
        private final T value;

        @JSONCreator
        private Response1(T value) {
            this.value = value;
        }
    }

    @Test
    public void test2() {
        BeanList beanList = JSON.parseObject("[{\"id\":123,\"name\":\"XXX\"}]", BeanList.class);
        assertEquals(1, beanList.size());
        Bean bean = beanList.get(0);
        assertEquals(123, bean.id);
        assertEquals("XXX", bean.name);
    }

    @Test
    public void test3() {
        Type type = TypeReference.collectionType(ArrayList.class, Bean.class);
        List<Bean> list = JSON.parseObject("[{\"id\":123,\"name\":\"XXX\"}]", type);
        assertEquals(1, list.size());
        Bean bean = list.get(0);
        assertEquals(123, bean.id);
        assertEquals("XXX", bean.name);
    }

    @Test
    public void test4() {
        Type type = TypeReference.mapType(ConcurrentHashMap.class, Key.class, Bean.class);
        ConcurrentHashMap<Key, Bean> map = JSON.parseObject("{\"value\":{\"id\":123,\"name\":\"XXX\"}}", type);
        assertEquals(1, map.size());
        Bean bean = map.get(new Key("value"));
        assertEquals(123, bean.id);
        assertEquals("XXX", bean.name);
    }

    @Test
    public void test5() {
        Type type = TypeReference.arrayType(Bean.class);
        Bean[] array = JSON.parseObject("[{\"id\":123,\"name\":\"XXX\"}]", type);
        assertEquals(1, array.length);
        Bean bean = array[0];
        assertEquals(123, bean.id);
        assertEquals("XXX", bean.name);
    }

    @Test
    public void test6() {
        Type type = TypeReference.parametricType(LinkedList.class, Bean.class);
        LinkedList<Bean> list = JSON.parseObject("[{\"id\":123,\"name\":\"XXX\"}]", type);
        assertEquals(1, list.size());
        Bean bean = list.get(0);
        assertEquals(123, bean.id);
        assertEquals("XXX", bean.name);
    }

    public static class Key {
        private final String value;

        @JSONCreator
        public Key(@JSONField(value = true) String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return Objects.equals(value, key.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    public static class Bean {
        public int id;
        public String name;
    }

    public static class BeanList
            extends ArrayList<Bean> {
    }
}
