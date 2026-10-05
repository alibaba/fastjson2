package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ReaderProviderCacheReviewTest {
    @Test
    public void clearingMixInsInvalidatesOnlyTargetReaders() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        ObjectReader<String> custom = ObjectReaders.ofString(value -> value);
        provider.register(String.class, custom);
        provider.registerIfAbsent(98761L, custom);
        provider.mixIn(Bean.class, MixIn.class);
        for (boolean fieldBased : new boolean[]{false, true}) {
            assertNotNull(provider.getObjectReader(Bean.class, fieldBased).getFieldReader("renamed"));
        }
        ObjectReader mixed = provider.getObjectReader(Bean.class);
        Thread thread = Thread.currentThread();
        ClassLoader original = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(new ClassLoader(original) { });
            provider.registerIfAbsent(98762L, mixed);
            assertSame(mixed, provider.getObjectReader(98762L));
            provider.cleanupMixIn();
            assertNull(provider.getObjectReader(98762L));
        } finally {
            thread.setContextClassLoader(original);
        }
        assertNull(provider.getObjectReader(98762L));
        assertSame(custom, provider.getObjectReader(String.class));
        assertSame(custom, provider.getObjectReader(98761L));
        for (boolean fieldBased : new boolean[]{false, true}) {
            ObjectReader reader = provider.getObjectReader(Bean.class, fieldBased);
            assertNotNull(reader.getFieldReader("value"));
            assertNull(reader.getFieldReader("renamed"));
        }
    }

    public static class Bean {
        public int value;
    }

    public static class MixIn {
        @JSONField(name = "renamed")
        public int value;
    }

    @Test
    public void hashLookupIsIsolatedBetweenProviders() {
        ObjectReaderProvider first = new ObjectReaderProvider();
        ObjectReaderProvider second = new ObjectReaderProvider();
        ObjectReader<String> firstReader = ObjectReaders.ofString(value -> "first:" + value);
        ObjectReader<String> secondReader = ObjectReaders.ofString(value -> "second:" + value);
        first.registerIfAbsent(123456789L, firstReader);
        second.registerIfAbsent(123456789L, secondReader);
        assertSame(firstReader, first.getObjectReader(123456789L));
        assertSame(secondReader, second.getObjectReader(123456789L));
    }

    @Test
    public void hashLookupUsesCurrentContextClassLoader() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        ObjectReader<String> firstReader = ObjectReaders.ofString(value -> "first:" + value);
        ObjectReader<String> secondReader = ObjectReaders.ofString(value -> "second:" + value);
        Thread thread = Thread.currentThread();
        ClassLoader original = thread.getContextClassLoader();
        ClassLoader first = new ClassLoader(original) { };
        ClassLoader second = new ClassLoader(original) { };
        try {
            thread.setContextClassLoader(first);
            provider.registerIfAbsent(987654321L, firstReader);
            thread.setContextClassLoader(second);
            provider.registerIfAbsent(987654321L, secondReader);
            thread.setContextClassLoader(first);
            assertSame(firstReader, provider.getObjectReader(987654321L));
            thread.setContextClassLoader(second);
            assertSame(secondReader, provider.getObjectReader(987654321L));
        } finally {
            thread.setContextClassLoader(original);
        }
    }
}
