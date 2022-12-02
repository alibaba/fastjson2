package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;

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

    public static class Bean {
        public int id;
        public String name;
    }

    public static class BeanList
            extends ArrayList<Bean> {
    }
}
