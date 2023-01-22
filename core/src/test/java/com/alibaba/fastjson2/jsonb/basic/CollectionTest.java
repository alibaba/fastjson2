package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.alibaba.fastjson2.JSONB.Constants.BC_ARRAY;
import static com.alibaba.fastjson2.JSONB.Constants.BC_ARRAY_FIX_0;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CollectionTest {
    /**
     * 0x94
     */
    @Test
    public void testEmpty() {
        Collection collection = Collections.emptyList();
        byte[] bytes = JSONB.toBytes(collection);
        assertEquals(1, bytes.length);
        assertEquals(BC_ARRAY_FIX_0, bytes[0]);
    }

    /**
     * 0x94 ~ 0xa3
     */
    @Test
    public void testFix_0_15() {
        for (int i = 0; i <= 15; i++) {
            Object[] array = new Object[i];
            for (int j = 0; j < array.length; j++) {
                array[j] = j;
            }
            Collection collection = Arrays.asList(array);
            byte[] bytes = JSONB.toBytes(collection);
            assertEquals(i + 1, bytes.length);
            assertEquals(BC_ARRAY_FIX_0 + i, bytes[0]);

            List parsed = (List) JSONB.parse(bytes);
            for (int j = 0; j < parsed.size(); j++) {
                assertEquals(array[j], parsed.get(j));
            }
        }
    }

    /**
     * 0xa4
     */
    @Test
    public void testFix_16_N() {
        for (int i = 16; i <= 47; i++) {
            Object[] array = new Object[i];
            for (int j = 0; j < array.length; j++) {
                array[j] = j;
            }
            Collection collection = Arrays.asList(array);
            byte[] bytes = JSONB.toBytes(collection);
            assertEquals(i + 2, bytes.length);
            assertEquals(BC_ARRAY, bytes[0]);

            List parsed = (List) JSONB.parse(bytes);
            for (int j = 0; j < parsed.size(); j++) {
                assertEquals(array[j], parsed.get(j));
            }
        }
    }
}
