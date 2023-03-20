package com.alibaba.fastjson2.support.hppc;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.carrotsearch.hppc.*;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteByteArrayAsBase64;
import static org.junit.jupiter.api.Assertions.*;

public class TestContainerSerializers {
    @Test
    public void testByteArray() {
        final byte[] input = new byte[]{
                (byte) -12, (byte) 0, (byte) -1, (byte) 0x7F
        };
        ByteArrayList array = new ByteArrayList();
        array.add(input);

        String str = JSON.toJSONString(array, WriteByteArrayAsBase64);
        assertEquals(JSON.toJSONString(input, WriteByteArrayAsBase64), str);

        ByteArrayList array2 = JSON.parseObject(str, ByteArrayList.class);
        assertArrayEquals(input, array2.toArray());
    }

    @Test
    public void testShortArray() {
        ShortArrayList array = new ShortArrayList();
        array.add((short) -12, (short) 0);
        String str = JSON.toJSONString(array);
        assertEquals("[-12,0]", str);

        ShortArrayList array2 = JSON.parseObject(str, ShortArrayList.class);
        assertArrayEquals(array.toArray(), array2.toArray());
    }

    @Test
    public void testIntArray() {
        IntArrayList array = new IntArrayList();
        array.add(-12, 0);
        String str = JSON.toJSONString(array);
        assertEquals("[-12,0]", str);

        IntArrayList array2 = JSON.parseObject(str, IntArrayList.class);
        assertArrayEquals(array.toArray(), array2.toArray());
    }

    @Test
    public void testIntSet() {
        IntHashSet set = new IntHashSet();
        set.addAll(1, 2);
        String str = JSON.toJSONString(set);
        assertTrue("[1,2]".equals(str) || "[2,1]".equals(str));

        IntHashSet set2 = JSON.parseObject(str, IntHashSet.class);
        assertEquals(set, set2);
    }

    @Test
    public void testLongArray() {
        LongArrayList array = new LongArrayList();
        array.add(-12L, 0L);
        String str = JSON.toJSONString(array);
        assertEquals("[-12,0]", str);

        LongArrayList array2 = JSON.parseObject(str, LongArrayList.class);
        assertArrayEquals(array.toArray(), array2.toArray());
    }

    @Test
    public void testLongSet() {
        LongHashSet set = new LongHashSet();
        set.addAll(1L, 2L);
        String str = JSON.toJSONString(set);
        assertTrue("[1,2]".equals(str) || "[2,1]".equals(str));

        LongHashSet set2 = JSON.parseObject(str, LongHashSet.class);
        assertEquals(set, set2);
    }

    @Test
    public void testCharArray() {
        CharArrayList array = new CharArrayList();
        array.add('a', 'b', 'c');
        String str = JSON.toJSONString(array);
        assertEquals("\"abc\"", str);

        CharArrayList array2 = JSON.parseObject(str, CharArrayList.class);
        assertArrayEquals(array.toArray(), array2.toArray());
    }

    @Test
    public void testCharSet() {
        CharHashSet set = new CharHashSet();
        set.addAll('d', 'e');
        String str = JSON.toJSONString(set);
        assertTrue("\"de\"".equals(str) || "\"ed\"".equals(str));

        CharHashSet set2 = JSON.parseObject(str, CharHashSet.class);
        assertEquals(set, set2);
    }

    @Test
    public void testFloatArray() {
        FloatArrayList array = new FloatArrayList();
        array.add(-12.25f, 0.5f);
        String str = JSON.toJSONString(array);
        assertEquals("[-12.25,0.5]", str);

        FloatArrayList array2 = JSON.parseObject(str, FloatArrayList.class);
        assertArrayEquals(array.toArray(), array2.toArray());
    }

    @Test
    public void testDoubleArray() {
        DoubleArrayList array = new DoubleArrayList();
        array.add(-12.25, 0.5);
        String str = JSON.toJSONString(array);
        assertEquals("[-12.25,0.5]", str);

        DoubleArrayList array2 = JSON.parseObject(str, DoubleArrayList.class);
        assertArrayEquals(array.toArray(), array2.toArray());
    }

    @Test
    public void testBitSet() throws Exception {
        BitSet bitset = new BitSet();
        bitset.set(1);
        bitset.set(4);

        String str = JSON.toJSONString(bitset);
        assertTrue(str.startsWith("[false,true,false,false,true"));

        String str1 = JSON.toJSONString(bitset, JSONWriter.Feature.WriteBooleanAsNumber);
        assertTrue(str1.startsWith("[0,1,0,0,1"));
    }
}
