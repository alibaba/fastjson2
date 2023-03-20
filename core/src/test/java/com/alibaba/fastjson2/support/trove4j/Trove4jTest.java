package com.alibaba.fastjson2.support.trove4j;

import com.alibaba.fastjson2.JSON;
import gnu.trove.list.array.*;
import gnu.trove.set.hash.TByteHashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;
import gnu.trove.set.hash.TShortHashSet;
import gnu.trove.stack.array.TByteArrayStack;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteByteArrayAsBase64;
import static org.junit.jupiter.api.Assertions.*;

public class Trove4jTest {
    @Test
    public void testByteArray() {
        final byte[] input = new byte[]{
                (byte) -12, (byte) 0, (byte) -1, (byte) 0x7F
        };
        TByteArrayList array = new TByteArrayList();
        array.add(input);

        String str = JSON.toJSONString(array, WriteByteArrayAsBase64);
        assertEquals(JSON.toJSONString(input, WriteByteArrayAsBase64), str);

        TByteArrayList array2 = JSON.parseObject(str, TByteArrayList.class);
        assertArrayEquals(input, array2.toArray());
    }

    @Test
    public void testByteStack() {
        final byte[] input = new byte[]{
                (byte) -12, (byte) 0, (byte) -1, (byte) 0x7F
        };
        TByteArrayStack array = new TByteArrayStack();
        for (int i = input.length - 1; i >= 0; i--) {
            array.push(input[i]);
        }

        String str = JSON.toJSONString(array, WriteByteArrayAsBase64);
        assertEquals(JSON.toJSONString(input, WriteByteArrayAsBase64), str);
    }

    @Test
    public void testByteSet() {
        TByteHashSet set = new TByteHashSet();
        set.add((byte) 1);
        set.add((byte) 2);
        String str = JSON.toJSONString(set);
        assertTrue("[1,2]".equals(str) || "[2,1]".equals(str));

        TByteHashSet set2 = JSON.parseObject(str, TByteHashSet.class);
        assertEquals(set, set2);
    }

    @Test
    public void testCharArray() {
        TCharArrayList array = new TCharArrayList();
        array.add('a');
        array.add('b');
        array.add('c');
        String str = JSON.toJSONString(array);
        assertEquals("\"abc\"", str);

        TCharArrayList array2 = JSON.parseObject(str, TCharArrayList.class);
        assertArrayEquals(array.toArray(), array2.toArray());
    }

    @Test
    public void testShortArray() {
        TShortArrayList array = new TShortArrayList();
        array.add((short) -12);
        array.add((short) 0);
        String str = JSON.toJSONString(array);
        assertEquals("[-12,0]", str);

        TShortArrayList array2 = JSON.parseObject(str, TShortArrayList.class);
        assertArrayEquals(array.toArray(), array2.toArray());
    }

    @Test
    public void testShortSet() {
        TShortHashSet set = new TShortHashSet();
        set.add((short) 1);
        set.add((short) 2);
        String str = JSON.toJSONString(set);
        assertTrue("[1,2]".equals(str) || "[2,1]".equals(str));

        TShortHashSet set2 = JSON.parseObject(str, TShortHashSet.class);
        assertEquals(set, set2);
    }

    @Test
    public void testIntArray() {
        TIntArrayList array = new TIntArrayList();
        array.add(-12);
        array.add(0);
        String str = JSON.toJSONString(array);
        assertEquals("[-12,0]", str);

        TIntArrayList array2 = JSON.parseObject(str, TIntArrayList.class);
        assertArrayEquals(array.toArray(), array2.toArray());
    }

    @Test
    public void testIntSet() {
        TIntHashSet set = new TIntHashSet();
        set.add(1);
        set.add(2);
        String str = JSON.toJSONString(set);
        assertTrue("[1,2]".equals(str) || "[2,1]".equals(str));

        TIntHashSet set2 = JSON.parseObject(str, TIntHashSet.class);
        assertEquals(set, set2);
    }

    @Test
    public void testLongArray() {
        TLongArrayList array = new TLongArrayList();
        array.add(-12);
        array.add(0);
        String str = JSON.toJSONString(array);
        assertEquals("[-12,0]", str);

        TLongArrayList array2 = JSON.parseObject(str, TLongArrayList.class);
        assertArrayEquals(array.toArray(), array2.toArray());
    }

    @Test
    public void testLongSet() {
        TLongHashSet set = new TLongHashSet();
        set.add(1L);
        set.add(2L);
        String str = JSON.toJSONString(set);
        assertTrue("[1,2]".equals(str) || "[2,1]".equals(str));

        TLongHashSet set2 = JSON.parseObject(str, TLongHashSet.class);
        assertEquals(set, set2);
    }

    @Test
    public void testFloatArray() {
        TFloatArrayList array = new TFloatArrayList();
        array.add(-12.25f);
        array.add(0.5f);
        String str = JSON.toJSONString(array);
        assertEquals("[-12.25,0.5]", str);

        TFloatArrayList array2 = JSON.parseObject(str, TFloatArrayList.class);
        assertArrayEquals(array.toArray(), array2.toArray());
    }

    @Test
    public void testDoubleArray() {
        TDoubleArrayList array = new TDoubleArrayList();
        array.add(-12.25);
        array.add(0.5);
        String str = JSON.toJSONString(array);
        assertEquals("[-12.25,0.5]", str);

        TDoubleArrayList array2 = JSON.parseObject(str, TDoubleArrayList.class);
        assertArrayEquals(array.toArray(), array2.toArray());
    }
}
