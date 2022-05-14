package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UseSingleQuotesTest {
    @Test
    public void test0() {
        assertEquals("'a\\'bc'",
                JSON.toJSONString("a'bc", JSONWriter.Feature.UseSingleQuotes)
        );

        assertEquals("'a\\'bc'",
                new String(
                        JSON.toJSONBytes("a'bc", JSONWriter.Feature.UseSingleQuotes)
                )
        );
    }

    @Test
    public void test1() {
        assertEquals("{'val':'a\\'bc'}",
                JSON.toJSONString(new JSONObject().fluentPut("val", "a'bc"), JSONWriter.Feature.UseSingleQuotes)
        );

        assertEquals("{'val':'a\\'bc'}",
                new String(
                        JSON.toJSONBytes(new JSONObject().fluentPut("val", "a'bc"), JSONWriter.Feature.UseSingleQuotes)
                )
        );
    }

    @Test
    public void test2() {
        Bean bean = new Bean();
        bean.val = "a'bc";
        assertEquals("{'val':'a\\'bc'}",
                JSON.toJSONString(bean, JSONWriter.Feature.UseSingleQuotes)
        );

        assertEquals("{'val':'a\\'bc'}",
                new String(JSON.toJSONBytes(bean, JSONWriter.Feature.UseSingleQuotes))
        );
    }

    public static class Bean {
        public String val;
    }
}
