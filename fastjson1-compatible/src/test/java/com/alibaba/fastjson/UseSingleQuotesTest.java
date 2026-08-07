package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UseSingleQuotesTest {
    @Test
    public void test0() {
        assertEquals("'a\\'bc'",
                JSON.toJSONString("a'bc", SerializerFeature.UseSingleQuotes)
        );

        assertEquals("'a\\'bc'",
                new String(
                        JSON.toJSONBytes("a'bc", SerializerFeature.UseSingleQuotes)
                )
        );
    }

    @Test
    public void test1() {
        assertEquals("{'val':'a\\'bc'}",
                JSON.toJSONString(new JSONObject().fluentPut("val", "a'bc"), SerializerFeature.UseSingleQuotes)
        );

        assertEquals("{'val':'a\\'bc'}",
                new String(
                        JSON.toJSONBytes(new JSONObject().fluentPut("val", "a'bc"), SerializerFeature.UseSingleQuotes)
                )
        );
    }

    @Test
    public void test2() {
        Bean bean = new Bean();
        bean.val = "a'bc";
        assertEquals("{'val':'a\\'bc'}",
                JSON.toJSONString(bean, SerializerFeature.UseSingleQuotes)
        );

        assertEquals("{'val':'a\\'bc'}",
                new String(JSON.toJSONBytes(bean, SerializerFeature.UseSingleQuotes))
        );
    }

    public static class Bean {
        public String val;
    }
}
