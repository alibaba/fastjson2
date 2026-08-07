package com.alibaba.fastjson.issues_compatible;

import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.parser.ParserConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue874 {
    @Test
    public void test() {
        Bean bean = JSONPath.read(
                "{\"val\":{\"id\":123}}",
                "$.val",
                Bean.class
        );
        assertEquals(123, bean.id);
    }

    @Test
    public void test1() {
        Bean bean = JSONPath.read(
                "{\"val\":{\"id\":123}}",
                "$.val",
                Bean.class,
                ParserConfig.getGlobalInstance()
        );
        assertEquals(123, bean.id);
    }

    public static class Bean {
        public int id;
    }
}
