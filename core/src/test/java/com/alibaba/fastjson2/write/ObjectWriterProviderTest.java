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

        JSON.register(Bean.class, (ObjectWriter) null);
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

    @Test
    public void testWriter1() {
        ObjectWriterProvider provider = new ObjectWriterProvider();

        BeanWriter writer = new BeanWriter();
        BeanWriter writer1 = new BeanWriter();

        assertNull(provider.register(Bean.class, writer, true));
        assertSame(writer, provider.register(Bean.class, writer, true));
        assertSame(writer, provider.register(Bean.class, writer1, true));
        assertSame(writer1, provider.register(Bean.class, writer1, true));

        assertFalse(provider.unregister(Bean.class, writer, true));
        assertTrue(provider.unregister(Bean.class, writer1, true));

        JSON.register(Bean.class, (ObjectWriter) null, true);
        assertNull(provider.register(Bean.class, writer, true));
        assertFalse(provider.unregister(Bean.class, writer1, true));
        assertTrue(provider.unregister(Bean.class, writer, true));

        assertNull(provider.registerIfAbsent(Bean.class, writer, true));
        assertSame(writer, provider.registerIfAbsent(Bean.class, writer, true));
        assertSame(writer, provider.registerIfAbsent(Bean.class, writer1, true));
        assertSame(writer, provider.registerIfAbsent(Bean.class, writer1, true));

        assertNull(JSON.register(Bean.class, writer, true));
        assertSame(writer, JSON.register(Bean.class, writer, true));
        assertSame(writer, JSON.register(Bean.class, writer1, true));

        assertSame(writer1, JSON.registerIfAbsent(Bean.class, writer1, true));
        assertSame(writer1, JSON.registerIfAbsent(Bean.class, writer, true));
        assertSame(writer1, JSON.registerIfAbsent(Bean.class, writer, true));
    }

    @Test
    public void testWriter2() {
        ObjectWriterProvider provider = new ObjectWriterProvider();

        BeanWriter writer = new BeanWriter();
        BeanWriter writer1 = new BeanWriter();

        assertNull(provider.register(Bean.class, writer, false));
        assertSame(writer, provider.register(Bean.class, writer, false));
        assertSame(writer, provider.register(Bean.class, writer1, false));
        assertSame(writer1, provider.register(Bean.class, writer1, false));

        assertFalse(provider.unregister(Bean.class, writer, false));
        assertTrue(provider.unregister(Bean.class, writer1, false));

        assertNull(provider.register(Bean.class, writer, false));
        assertFalse(provider.unregister(Bean.class, writer1, false));
        assertTrue(provider.unregister(Bean.class, writer, false));

        assertNull(provider.registerIfAbsent(Bean.class, writer, false));
        assertSame(writer, provider.registerIfAbsent(Bean.class, writer, false));
        assertSame(writer, provider.registerIfAbsent(Bean.class, writer1, false));
        assertSame(writer, provider.registerIfAbsent(Bean.class, writer1, false));

        JSON.register(Bean.class, (ObjectWriter) null, false);
        assertNull(JSON.register(Bean.class, writer, false));
        assertSame(writer, JSON.register(Bean.class, writer, false));
        assertSame(writer, JSON.register(Bean.class, writer1, false));

        assertSame(writer1, JSON.registerIfAbsent(Bean.class, writer1, false));
        assertSame(writer1, JSON.registerIfAbsent(Bean.class, writer, false));
        assertSame(writer1, JSON.registerIfAbsent(Bean.class, writer, false));
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
