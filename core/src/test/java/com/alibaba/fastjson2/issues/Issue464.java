package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue464 {
    @Test
    public void test0() {
        String str = "{\"test\":[{\"demo\":\"\"}]}";
        System.out.println("str = " + str);
        TestParam param = JSON.parseObject(str, TestParam.class);
        System.out.println("list = " + param);
    }

    @Data
    static class TestParam {
        List<Demo> test;
    }

    @Data
    static class Demo{
        private Integer[] demo;
    }

    @Test
    public void test1() {
        assertNull(JSON.parseObject("\"\"", byte[].class));
        assertNull(JSON.parseObject("\"\"", short[].class));
        assertNull(JSON.parseObject("\"\"", int[].class));
        assertNull(JSON.parseObject("\"\"", long[].class));
        assertNull(JSON.parseObject("\"\"", float[].class));
        assertNull(JSON.parseObject("\"\"", double[].class));
        assertNull(JSON.parseObject("\"\"", boolean[].class));

        assertNull(JSON.parseObject("\"\"", Byte[].class));
        assertNull(JSON.parseObject("\"\"", Short[].class));
        assertNull(JSON.parseObject("\"\"", Integer[].class));
        assertNull(JSON.parseObject("\"\"", Long[].class));
        assertNull(JSON.parseObject("\"\"", Float[].class));
        assertNull(JSON.parseObject("\"\"", Double[].class));
        assertNull(JSON.parseObject("\"\"", Boolean[].class));

        assertNull(JSON.parseObject("\"\"", Object[].class));
    }
}
