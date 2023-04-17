package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProxyFactoryTest {
    @Test
    public void test() {
        JSONObject object = new JSONObject();
        Bean bean = TypeUtils.newProxyInstance(Bean.class, object);
        bean.setId(123);
        assertEquals(123, bean.getId());
    }

    public interface Bean {
        int getId();

        void setId(int value);
    }

    @Test
    public void test1() {
        JSONObject object = new JSONObject();
        Bean1 bean = TypeUtils.newProxyInstance(Bean1.class, object);
        bean.setId((byte) 123);
        assertEquals(123, bean.getId());
    }

    public interface Bean1 {
        byte getId();

        void setId(byte value);
    }

    @Test
    public void test2() {
        JSONObject object = new JSONObject();
        Bean2 bean = TypeUtils.newProxyInstance(Bean2.class, object);
        bean.setId((short) 123);
        assertEquals(123, bean.getId());
    }

    public interface Bean2 {
        short getId();

        void setId(short value);
    }

    @Test
    public void test3() {
        JSONObject object = new JSONObject();
        Bean3 bean = TypeUtils.newProxyInstance(Bean3.class, object);
        bean.setId(123);
        assertEquals(123, bean.getId());
    }

    public interface Bean3 {
        long getId();

        void setId(long value);
    }

    @Test
    public void test4() {
        JSONObject object = new JSONObject();
        Bean4 bean = TypeUtils.newProxyInstance(Bean4.class, object);
        bean.setId(123);
        assertEquals(123, bean.getId());
    }

    public interface Bean4 {
        float getId();

        void setId(float value);
    }

    @Test
    public void test5() {
        JSONObject object = new JSONObject();
        Bean5 bean = TypeUtils.newProxyInstance(Bean5.class, object);
        bean.setId(123);
        assertEquals(123, bean.getId());
    }

    public interface Bean5 {
        double getId();

        void setId(double value);
    }

    @Test
    public void test6() {
        JSONObject object = new JSONObject();
        Bean6 bean = TypeUtils.newProxyInstance(Bean6.class, object);
        bean.setId(true);
        assertEquals(true, bean.getId());
    }

    public interface Bean6 {
        boolean getId();

        void setId(boolean value);
    }

    @Test
    public void test7() {
        JSONObject object = new JSONObject();
        Bean7 bean = TypeUtils.newProxyInstance(Bean7.class, object);
        bean.setId('a');
        assertEquals('a', bean.getId());
    }

    public interface Bean7 {
        char getId();

        void setId(char value);
    }

    @Test
    public void test8() {
        JSONObject object = new JSONObject();
        Bean8 bean = TypeUtils.newProxyInstance(Bean8.class, object);
        bean.setId("123");
        assertEquals("123", bean.getId());
    }

    public interface Bean8 {
        String getId();

        void setId(String value);
    }

    @Test
    public void test9() {
        JSONObject object = new JSONObject();
        Bean9 bean = TypeUtils.newProxyInstance(Bean9.class, object);
        bean.setId("123");
        assertEquals("123", bean.getId());
    }

    public interface Bean9 {
        String getId();

        Bean9 setId(String value);
    }
}
