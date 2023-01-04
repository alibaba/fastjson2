package com.alibaba.fastjson2.internal.trove;

import com.alibaba.fastjson2.internal.trove.map.hash.TLongIntHashMap;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TLongIntHashMapTest {
    @Test
    public void test_0() {
        gnu.trove.map.hash.TLongIntHashMap map0 = new gnu.trove.map.hash.TLongIntHashMap(16);

        TLongIntHashMap map = new TLongIntHashMap();

        map.put(1000L, 0);
        map.put(1001L, 1);

        assertEquals("{1001=1, 1000=0}", map.toString());
        assertEquals(2, map.size());

        assertEquals(0, map.get(1000L));
        assertEquals(1, map.get(1001L));
        assertEquals(-1, map.get(1000000L));
    }

    @Test
    public void test_1() {
        TLongIntHashMap map = new TLongIntHashMap();

        Random random = new Random();
        for (int i = 0; i < 1000_000; ++i) {
            long value = random.nextInt(1000 * 100);
            if (value == 0) {
                continue;
            }
            map.put(value, 0);
            assertEquals(0, map.get(value));
        }
    }

    @Test
    public void test_3() {
        TLongIntHashMap map = new TLongIntHashMap();

        Random random = new Random();
        for (int i = 0; i < 1000_000; ++i) {
            long value = random.nextInt(1000 * 100);
            if (value == 0) {
                continue;
            }
            map.putIfAbsent(value, 0);
            assertEquals(0, map.get(value));
        }
    }

    @Test
    public void test_2() {
        TLongIntHashMap map = new TLongIntHashMap();
        assertEquals(102, map.putIfAbsent(101, 102));
        assertEquals(102, map.putIfAbsent(101, 103));
    }
}
