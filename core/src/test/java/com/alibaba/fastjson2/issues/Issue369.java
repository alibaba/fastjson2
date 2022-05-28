package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class Issue369 {
    @Test
    public void test() {
        Class<?>[] arrays = new Class[]{String.class, int.class, String.class, BigDecimal.class};
        Wrapper wrapper = new Wrapper();
        wrapper.parameterTypes = arrays;

        String s = JSON.toJSONString(wrapper);
        Wrapper parse = JSON.parseObject(s, Wrapper.class, JSONReader.Feature.SupportClassForName);
        assertArrayEquals(wrapper.parameterTypes, parse.parameterTypes);
    }

    public static class Wrapper {
        public Class<?>[] parameterTypes;
    }

    @Test
    public void test2() {
        Class<?>[] arrays = new Class[]{String.class, int.class, String.class, BigDecimal.class};
        Wrapper2 wrapper = new Wrapper2();
        wrapper.parameterTypes = arrays;

        String s = JSON.toJSONString(wrapper);
        Wrapper2 parse = JSON.parseObject(s, Wrapper2.class, JSONReader.Feature.SupportClassForName);
        assertArrayEquals(wrapper.parameterTypes, parse.parameterTypes);
    }

    public static class Wrapper2 {
        public Class[] parameterTypes;
    }
}
