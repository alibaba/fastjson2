package com.alibaba.fastjson2;

import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    public void test1() {
        JSONFactory.setUseJacksonAnnotation(false);
        assertFalse(JSONFactory.isUseJacksonAnnotation());
        JSONFactory.setUseJacksonAnnotation(true);
        assertTrue(JSONFactory.isUseJacksonAnnotation());

        ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
        JSONWriter.Context context = JSONFactory.createWriteContext(provider);
        assertSame(provider, context.getProvider());
    }
}
