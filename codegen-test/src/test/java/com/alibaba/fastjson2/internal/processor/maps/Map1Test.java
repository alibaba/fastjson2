package com.alibaba.fastjson2.internal.processor.maps;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Map1Test {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.v01 = new HashMap<>();
        bean.v01.put("123", "abc");

        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.v01, bean1.v01);
    }

    @JSONCompiled
    public static class Bean {
        public Map<String, String> v01;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.v01 = new HashMap<>();
        bean.v01.put("123", 123);

        String str = JSON.toJSONString(bean);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.v01, bean1.v01);
    }

    @JSONCompiled
    public static class Bean1 {
        public Map<String, Integer> v01;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.v01 = new HashMap<>();
        bean.v01.put("123", 123L);

        String str = JSON.toJSONString(bean);
        Bean2 bean1 = JSON.parseObject(str, Bean2.class);
        assertEquals(bean.v01, bean1.v01);
    }

    @JSONCompiled
    public static class Bean2 {
        public Map<String, Long> v01;
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.v01 = new HashMap<>();
        bean.v01.put("101", true);
        bean.v01.put("102", false);

        String str = JSON.toJSONString(bean);
        Bean3 bean1 = JSON.parseObject(str, Bean3.class);
        assertEquals(bean.v01, bean1.v01);
    }

    @JSONCompiled
    public static class Bean3 {
        public Map<String, Boolean> v01;
    }

    @Test
    public void test4() {
        Bean4 bean = new Bean4();
        bean.v01 = new HashMap<>();
        bean.v01.put("101", 1F);
        bean.v01.put("102", 2F);

        String str = JSON.toJSONString(bean);
        Bean4 bean1 = JSON.parseObject(str, Bean4.class);
        assertEquals(bean.v01, bean1.v01);
    }

    @JSONCompiled
    public static class Bean4 {
        public Map<String, Float> v01;
    }

    @Test
    public void test5() {
        Bean5 bean = new Bean5();
        bean.v01 = new HashMap<>();
        bean.v01.put("101", 1D);
        bean.v01.put("102", 2D);

        String str = JSON.toJSONString(bean);
        Bean5 bean1 = JSON.parseObject(str, Bean5.class);
        assertEquals(bean.v01, bean1.v01);
    }

    @JSONCompiled
    public static class Bean5 {
        public Map<String, Double> v01;
    }

    @Test
    public void test6() {
        Bean6 bean = new Bean6();
        bean.v01 = new HashMap<>();
        bean.v01.put("101", BigDecimal.valueOf(101));
        bean.v01.put("102", BigDecimal.valueOf(102));

        String str = JSON.toJSONString(bean);
        Bean6 bean1 = JSON.parseObject(str, Bean6.class);
        assertEquals(bean.v01, bean1.v01);
    }

    @JSONCompiled
    public static class Bean6 {
        public Map<String, BigDecimal> v01;
    }

    @Test
    public void test7() {
        Bean7 bean = new Bean7();
        bean.v01 = new HashMap<>();
        bean.v01.put("101", BigInteger.valueOf(101));
        bean.v01.put("102", BigInteger.valueOf(102));

        String str = JSON.toJSONString(bean);
        Bean7 bean1 = JSON.parseObject(str, Bean7.class);
        assertEquals(bean.v01, bean1.v01);
    }

    @JSONCompiled
    public static class Bean7 {
        public Map<String, BigInteger> v01;
    }

    @Test
    public void test8() {
        Bean8 bean = new Bean8();
        bean.v01 = new HashMap<>();
        bean.v01.put("101", UUID.randomUUID());
        bean.v01.put("102", UUID.randomUUID());

        String str = JSON.toJSONString(bean);
        Bean8 bean1 = JSON.parseObject(str, Bean8.class);
        assertEquals(bean.v01, bean1.v01);
    }

    @JSONCompiled
    public static class Bean8 {
        public Map<String, UUID> v01;
    }
}
