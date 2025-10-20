package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DisableStringArrayUnwrappingTest {
    @Test
    public void test() {
        String str = "{\"name\":[\"abc\"]}";
        assertEquals("abc", JSON.parseObject(str, Bean.class).name);
        assertEquals("[\"abc\"]",
                JSON.parseObject(str, Bean.class, JSONReader.Feature.DisableStringArrayUnwrapping).name);
    }

    public static class Bean {
        public String name;
    }
}
