package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue564 {
    @Test
    public void test() {
        char a = '1';
        String str = TypeUtils.cast(a, String.class);
        char b = TypeUtils.cast(str, char.class);
        assertEquals(str, Character.toString(a));
        assertEquals(a, b);
    }
}
