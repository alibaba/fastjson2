package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class JSONFactoryTest {
    @Test
    public void contextReaderCreator() {
        JSONFactory.setContextObjectReaderProvider(null);
        assertSame(JSONFactory.defaultObjectReaderProvider, JSONFactory.getDefaultObjectReaderProvider());
    }

    @Test
    public void contextJSONPathCompiler() {
        JSONFactory.setContextJSONPathCompiler(null);
        assertSame(JSONFactory.defaultJSONPathCompiler, JSONFactory.getDefaultJSONPathCompiler());
    }

    @Test
    public void test() {
        new JSONFactory.Utils();
    }
}
