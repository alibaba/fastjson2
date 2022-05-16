package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BitSetTest {
    @Test
    public void test0() {
        BitSet bitSet1 = new BitSet();
        bitSet1.set(1);
        bitSet1.set(5);
        bitSet1.set(100);

        byte[] jsonbBytes = JSONB.toBytes(bitSet1);

        BitSet bitSet2 = JSONB.parseObject(jsonbBytes, BitSet.class);

        assertEquals(bitSet1, bitSet2);
    }
}
