package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpecialTest0 {
    @Test
    public void test_0() throws Exception {
        String str = "$[1].params.com\\.alibaba\\.carts\\.shared\\.param\\.ExtraParams.wirelessParams.wirelessClientConfig";
        JSONPath path = JSONPath.of(str);
        Class type = path.getClass();
        Field field = type.getDeclaredField("segments");
        field.setAccessible(true);
        List segments = (List) field.get(path);
        assertEquals(5, segments.size());
    }
}
