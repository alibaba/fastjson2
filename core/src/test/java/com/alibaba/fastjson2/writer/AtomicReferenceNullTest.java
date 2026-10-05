package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AtomicReferenceNullTest {
    @Test
    public void emptyReferenceWritesExactlyOneNull() {
        AtomicReference<Object> reference = new AtomicReference<>();
        assertEquals("null", JSON.toJSONString(reference));
        assertArrayEquals(JSONB.toBytes(null), JSONB.toBytes(reference));
    }

    @Test
    public void emptyReferenceDoesNotCorruptFollowingArrayValue() {
        Object[] values = {new AtomicReference<>(), 123};
        assertEquals("[null,123]", JSON.toJSONString(values));
        assertArrayEquals(JSONB.toBytes(new Object[] {null, 123}), JSONB.toBytes(values));
    }
}
