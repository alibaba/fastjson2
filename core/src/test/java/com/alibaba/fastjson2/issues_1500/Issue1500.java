package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1500 {
    @Test
    public void test() {
        List<TestEnum> enums = Arrays.asList(TestEnum.A, TestEnum.B);
        JSONArray array = JSON.parseArray(JSON.toJSONString(enums));
        List<TestEnum> result = array.to(new ParameterizedTypeImpl(new Class[]{TestEnum.class}, null, List.class));
        assertEquals(2, result.size());
        assertEquals(TestEnum.A, result.get(0));
        assertEquals(TestEnum.B, result.get(1));
    }

    enum TestEnum {
        A,
        B
    }

    @Test
    public void test1() {
        List<TestEnum1> enums = Arrays.asList(TestEnum1.A, TestEnum1.B, TestEnum1.C);
        JSONArray array = JSON.parseArray(JSON.toJSONString(enums));
        List<TestEnum1> result = array.to(new ParameterizedTypeImpl(new Class[]{TestEnum1.class}, null, List.class));
        assertEquals(3, result.size());
        assertEquals(TestEnum1.A, result.get(0));
        assertEquals(TestEnum1.B, result.get(1));
        assertEquals(TestEnum1.C, result.get(2));
    }

    enum TestEnum1 {
        A,
        B,
        C
    }
}
