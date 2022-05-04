package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SafeModeTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName);
        Object object = JSON.parse(str);
        assertTrue(object instanceof Map);
        assertTrue(JSON.parse(str, JSONReader.Feature.SupportAutoType) instanceof Map);
    }

    public static class Bean {

    }
}
