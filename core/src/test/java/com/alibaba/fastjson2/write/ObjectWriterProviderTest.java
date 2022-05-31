package com.alibaba.fastjson2.write;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectWriterProviderTest {
    @Test
    public void testWriter() {
        ObjectWriterProvider provider = new ObjectWriterProvider();

        BeanWriter writer = new BeanWriter();
        BeanWriter writer1 = new BeanWriter();

        assertNull(provider.register(Bean.class, writer));
        assertSame(writer, provider.register(Bean.class, writer));
        assertSame(writer, provider.register(Bean.class, writer1));
        assertSame(writer1, provider.register(Bean.class, writer1));

        assertFalse(provider.unregister(Bean.class, writer));
        assertTrue(provider.unregister(Bean.class, writer1));

        assertNull(provider.register(Bean.class, writer));
        assertFalse(provider.unregister(Bean.class, writer1));
        assertTrue(provider.unregister(Bean.class, writer));

        assertNull(provider.registerIfAbsent(Bean.class, writer));
        assertSame(writer, provider.registerIfAbsent(Bean.class, writer));
        assertSame(writer, provider.registerIfAbsent(Bean.class, writer1));
        assertSame(writer, provider.registerIfAbsent(Bean.class, writer1));

        assertNull(JSON.register(Bean.class, writer));
        assertSame(writer, JSON.register(Bean.class, writer));
        assertSame(writer, JSON.register(Bean.class, writer1));

        assertSame(writer1, JSON.registerIfAbsent(Bean.class, writer1));
        assertSame(writer1, JSON.registerIfAbsent(Bean.class, writer));
        assertSame(writer1, JSON.registerIfAbsent(Bean.class, writer));

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

    public static class BeanWriter
            implements ObjectWriter {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        }
    }

    public static class MyModoule
            implements ObjectWriterModule {
    }
}
