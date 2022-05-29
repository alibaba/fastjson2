package com.alibaba.fastjson2.fieldbased;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FieldBasedTest2 {
    @Test
    public void test_0() {
        A a = new A();
        a.v3 = 'A';
        a.v7 = new int[]{101};
        a.v8 = Collections.emptyList();
        a.v9 = Collections.emptyMap();
        a.v10 = Collections.emptyList();

        String str = JSON.toJSONString(a, JSONWriter.Feature.FieldBased);
        assertEquals("{\"v0\":0,\"v1\":0.0,\"v10\":[],\"v2\":0.0,\"v3\":\"A\",\"v4\":0,\"v5\":0,\"v6\":false,\"v7\":[101],\"v8\":[],\"v9\":{}}", str);
    }

    static class A {
        long v0;
        float v1;
        double v2;
        char v3;
        byte v4;
        short v5;
        boolean v6;
        int[] v7;
        List v8;
        Map v9;
        Collection v10;
    }
}
