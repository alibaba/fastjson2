package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class JSONBStreamReviewTest {
    @Test
    void zeroByteBulkReadFallsBackWithoutConsumingFollowingRecord() throws Exception {
        byte[] record = JSONB.toBytes("record");
        byte[] input = Arrays.copyOf(record, record.length + 1);
        input[record.length] = 42;
        ByteArrayInputStream stream = new ByteArrayInputStream(input) {
            @Override
            public synchronized int read(byte[] b, int off, int len) {
                return 0;
            }
        };
        assertEquals("record", JSONB.parseObject(stream, record.length, String.class));
        assertEquals(42, stream.read());
    }

    @Test
    void exactLengthReadsAcceptShortReadsAndLeaveNextRecord() throws Exception {
        byte[] record = JSONB.toBytes("a JSONB record");
        byte[] input = Arrays.copyOf(record, record.length + 1);
        input[record.length] = 42;
        for (boolean context : new boolean[] {false, true}) {
            ByteArrayInputStream stream = new ByteArrayInputStream(input) {
                @Override
                public synchronized int read(byte[] b, int off, int len) {
                    return super.read(b, off, Math.min(2, len));
                }
            };
            String value = context
                    ? JSONB.parseObject(stream, record.length, String.class, JSONFactory.createReadContext())
                    : JSONB.parseObject(stream, record.length, String.class);
            assertEquals("a JSONB record", value);
            assertEquals(42, stream.read());
        }
        assertThrows(IllegalArgumentException.class, () -> JSONB.parseObject(
                new ByteArrayInputStream(new byte[0]), record.length, String.class));
    }
}
