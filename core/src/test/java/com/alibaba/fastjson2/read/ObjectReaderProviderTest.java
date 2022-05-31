package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectReaderProviderTest {
    @Test
    public void testReader() {
        ObjectReaderProvider provider = new ObjectReaderProvider();

        BeanReader reader = new BeanReader();
        BeanReader reader1 = new BeanReader();

        assertNull(provider.unregisterObjectReader(Bean.class));
        assertNull(provider.register(Bean.class, reader));

        assertSame(reader, provider.register(Bean.class, reader1));
        assertFalse(provider.unregisterObjectReader(Bean.class, reader));
        assertTrue(provider.unregisterObjectReader(Bean.class, reader1));

        assertNull(provider.register(Bean.class, reader1));
        assertSame(reader1, provider.unregisterObjectReader(Bean.class));
        assertNull(provider.unregisterObjectReader(Bean.class));

        assertNull(JSON.register(Bean.class, reader));
        assertSame(reader, JSON.register(Bean.class, reader));
        assertSame(reader, JSON.register(Bean.class, reader1));

        assertSame(reader1, JSON.registerIfAbsent(Bean.class, reader1));
        assertSame(reader1, JSON.registerIfAbsent(Bean.class, reader1));
        assertSame(reader1, JSON.registerIfAbsent(Bean.class, reader));

        MyModoule modoule = new MyModoule();
        MyModoule modoule1 = new MyModoule();

        assertTrue(provider.register(modoule));
        assertFalse(provider.register(modoule));

        assertFalse(provider.unregister(modoule1));

        assertTrue(provider.register(modoule1));
        assertFalse(provider.register(modoule1));

        assertTrue(provider.unregister(modoule));
        assertTrue(provider.unregister(modoule1));

        assertFalse(provider.unregister(modoule));
        assertFalse(provider.unregister(modoule1));
    }

    public static class Bean {
    }

    public static class BeanReader
            implements ObjectReader {
        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            return null;
        }
    }

    public static class MyModoule
            implements ObjectReaderModule {
    }
}
