package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3435 {
    record Data6(int a, int b, int c, int d, int e, int f) {}
    record Data7(int a, int b, int c, int d, int e, int f, int g) {}

    @Test
    public void test6() {
        String s6 = "{\"a\":0,\"b\":0,\"c\":0,\"d\":0,\"e\":0,\"f\":0}";
        Data6 d6 = JSON.parseObject(s6, Data6.class);
        assertEquals("{\"a\":0,\"b\":0,\"c\":0,\"d\":0,\"e\":0,\"f\":0}", JSON.toJSONString(d6));
    }

    @Test
    public void test7() {
        String s7 = "{\"a\":0,\"b\":0,\"c\":0,\"d\":0,\"e\":0,\"f\":0,\"g\":0}";
        Data7 d7 = JSON.parseObject(s7, Data7.class);
        assertEquals("{\"a\":0,\"b\":0,\"c\":0,\"d\":0,\"e\":0,\"f\":0,\"g\":0}", JSON.toJSONString(d7));
    }
}
