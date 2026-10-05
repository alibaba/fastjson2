package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteBufferRegionReviewTest {
    @Test
    public void onlyRemainingBytesAreSerializedWithoutChangingPosition() {
        ByteBuffer heap = ByteBuffer.wrap(new byte[] {0, 1, 2, 3});
        heap.position(1);
        heap.limit(3);
        ByteBuffer direct = ByteBuffer.allocateDirect(4);
        direct.put(new byte[] {0, 1, 2, 3});
        direct.position(1);
        direct.limit(3);
        for (ByteBuffer buffer : new ByteBuffer[] {heap, heap.slice(), heap.asReadOnlyBuffer(), direct}) {
            int position = buffer.position();
            buffer.mark();
            assertArrayEquals(new byte[] {1, 2}, JSON.parseObject(JSON.toJSONString(buffer), byte[].class));
            assertArrayEquals(new byte[] {1, 2}, JSONB.parseObject(JSONB.toBytes(buffer), byte[].class));
            assertEquals(position, buffer.position());
            buffer.reset();
            assertEquals(position, buffer.position());
        }
    }
}
