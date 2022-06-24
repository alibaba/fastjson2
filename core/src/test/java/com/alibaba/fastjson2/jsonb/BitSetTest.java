package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BitSetTest {
    @Test
    public void testjsonb() {
        BitSet bitSet1 = new BitSet();
        bitSet1.set(1);
        bitSet1.set(5);
        bitSet1.set(100);

        byte[] jsonbBytes = JSONB.toBytes(bitSet1);

        BitSet bitSet2 = JSONB.parseObject(jsonbBytes, BitSet.class);

        assertEquals(bitSet1, bitSet2);
    }

    @Test
    public void test1() {
        BitSet bitSet1 = new BitSet();
        bitSet1.set(1);
        bitSet1.set(5);
        bitSet1.set(100);

        String str = JSON.toJSONString(bitSet1);

        System.out.println(str);

        BitSet bitSet2 = JSON.parseObject(str, BitSet.class);

        assertEquals(bitSet1, bitSet2);
    }
}
