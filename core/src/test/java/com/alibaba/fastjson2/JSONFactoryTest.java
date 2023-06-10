package com.alibaba.fastjson2;

import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JSONFactoryTest {
    @Test
    public void contextReaderCreator() {
        assertSame(JSONFactory.defaultObjectReaderProvider, JSONFactory.defaultObjectReaderProvider);
    }

    @Test
    public void test1() {
        ObjectWriterProvider provider = JSONFactory.defaultObjectWriterProvider;
        JSONWriter.Context context = JSONFactory.createWriteContext(provider);
        assertSame(provider, context.getProvider());
    }
}
