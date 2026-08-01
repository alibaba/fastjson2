package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;

@Tag("util")
public class ParameterizedTypeImplTest {
    @Test
    public void test() {
        ParameterizedTypeImpl paramType = new ParameterizedTypeImpl(Map.class, String.class, Object.class);
        assertSame(Map.class, paramType.getRawType());
    }
}
