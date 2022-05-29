package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TypeConvertTest {
    @Test
    public void test_0() {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();

        assertEquals(0F, provider.getTypeConvert(String.class, float.class).apply(""));
        assertEquals(0D, provider.getTypeConvert(String.class, double.class).apply(""));
        assertEquals(Byte.valueOf((byte) 0), provider.getTypeConvert(String.class, byte.class).apply(""));
        assertEquals(Short.valueOf((short) 0), provider.getTypeConvert(String.class, short.class).apply(""));
        assertEquals(0, provider.getTypeConvert(String.class, int.class).apply(""));
        assertEquals(0L, provider.getTypeConvert(String.class, long.class).apply(""));
        assertEquals(Boolean.FALSE, provider.getTypeConvert(String.class, boolean.class).apply(""));

        assertNull(provider.getTypeConvert(String.class, Integer.class).apply(""));
        assertNull(provider.getTypeConvert(String.class, Long.class).apply(""));

        assertEquals(0F, provider.getTypeConvert(String.class, float.class).apply("null"));
        assertEquals(0D, provider.getTypeConvert(String.class, double.class).apply("null"));
        assertEquals(Byte.valueOf((byte) 0), provider.getTypeConvert(String.class, byte.class).apply("null"));
        assertEquals(Short.valueOf((short) 0), provider.getTypeConvert(String.class, short.class).apply("null"));
        assertEquals(0, provider.getTypeConvert(String.class, int.class).apply("null"));
        assertEquals(0L, provider.getTypeConvert(String.class, long.class).apply("null"));
        assertEquals(Boolean.FALSE, provider.getTypeConvert(String.class, boolean.class).apply("null"));

        assertNull(provider.getTypeConvert(String.class, Integer.class).apply("null"));
        assertNull(provider.getTypeConvert(String.class, Long.class).apply("null"));

        assertEquals(0F, provider.getTypeConvert(String.class, float.class).apply(null));
        assertEquals(0D, provider.getTypeConvert(String.class, double.class).apply(null));
        assertEquals(Byte.valueOf((byte) 0), provider.getTypeConvert(String.class, byte.class).apply(null));
        assertEquals(Short.valueOf((short) 0), provider.getTypeConvert(String.class, short.class).apply(null));
        assertEquals(0, provider.getTypeConvert(String.class, int.class).apply(null));
        assertEquals(0L, provider.getTypeConvert(String.class, long.class).apply(null));
        assertEquals(Boolean.FALSE, provider.getTypeConvert(String.class, boolean.class).apply(null));

        assertNull(provider.getTypeConvert(String.class, Integer.class).apply(null));
        assertNull(provider.getTypeConvert(String.class, Long.class).apply(null));

        assertEquals(1, provider.getTypeConvert(String.class, Integer.class).apply("1"));
        assertEquals(1L, provider.getTypeConvert(String.class, Long.class).apply("1"));
    }
}
