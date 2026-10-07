package com.alibaba.fastjson2.write;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("writer")
public class ByteBufferTest {
    @Test
    public void test() {
        byte[] bytes = new byte[]{1, 2, 3};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        String str = JSON.toJSONString(buffer);
        ByteBuffer buffer1 = JSON.parseObject(str, ByteBuffer.class);
        assertArrayEquals(bytes, buffer1.array());
    }

    @Test
    public void test1() {
        byte[] bytes = new byte[]{1, 2, 3};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        String str = JSON.toJSONString(buffer, JSONWriter.Feature.WriteByteArrayAsBase64);
        ByteBuffer buffer1 = JSON.parseObject(str, ByteBuffer.class, JSONReader.Feature.Base64StringAsByteArray);
        assertArrayEquals(bytes, buffer1.array());
    }

    @Test
    public void testjsonb() {
        byte[] bytes = new byte[]{1, 2, 3};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        byte[] jsonbBytes = JSONB.toBytes(buffer, JSONWriter.Feature.WriteClassName);
        System.out.println(JSONB.toJSONString(jsonbBytes));
        ByteBuffer buffer1 = (ByteBuffer) JSONB.parseObject(jsonbBytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertArrayEquals(bytes, buffer1.array());
    }

    @Test
    public void testDirectByteBuffer() {
        // Test DirectByteBuffer serialization (allocateDirect creates a DirectByteBuffer)
        byte[] bytes = new byte[]{1, 2, 3};
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(3);
        directBuffer.put(bytes);
        directBuffer.flip();

        String str = JSON.toJSONString(directBuffer);
        assertEquals("[1,2,3]", str);

        ByteBuffer buffer1 = JSON.parseObject(str, ByteBuffer.class);
        assertArrayEquals(bytes, buffer1.array());
    }

    @Test
    public void testReadOnlyByteBuffer() {
        // Test read-only ByteBuffer serialization
        byte[] bytes = new byte[]{1, 2, 3};
        ByteBuffer buffer = ByteBuffer.wrap(bytes).asReadOnlyBuffer();

        String str = JSON.toJSONString(buffer);
        assertEquals("[1,2,3]", str);

        ByteBuffer buffer1 = JSON.parseObject(str, ByteBuffer.class);
        assertArrayEquals(bytes, buffer1.array());
    }

    @Test
    public void testBufferViews() {
        byte[] bytes = new byte[]{9, 1, 2, 3, 8};

        ByteBuffer heap = ByteBuffer.wrap(bytes, 1, 3);

        ByteBuffer parent = ByteBuffer.wrap(bytes);
        parent.position(1);
        parent.limit(4);
        ByteBuffer slice = parent.slice();

        ByteBuffer direct = ByteBuffer.allocateDirect(bytes.length);
        direct.put(bytes);
        direct.position(1);
        direct.limit(4);

        ByteBuffer readOnly = ByteBuffer.wrap(bytes).asReadOnlyBuffer();
        readOnly.position(1);
        readOnly.limit(4);

        assertRemainingRoundTrip(heap);
        assertRemainingRoundTrip(slice);
        assertRemainingRoundTrip(direct);
        assertRemainingRoundTrip(readOnly);
    }

    private static void assertRemainingRoundTrip(ByteBuffer buffer) {
        int position = buffer.position();
        int limit = buffer.limit();
        byte[] expected = new byte[]{1, 2, 3};

        String str = JSON.toJSONString(buffer);
        assertEquals("[1,2,3]", str);
        ByteBuffer parsed = JSON.parseObject(str, ByteBuffer.class);
        assertArrayEquals(expected, parsed.array());
        assertEquals(position, buffer.position());
        assertEquals(limit, buffer.limit());

        byte[] jsonb = JSONB.toBytes(buffer);
        ByteBuffer parsedJSONB = JSONB.parseObject(jsonb, ByteBuffer.class);
        assertArrayEquals(expected, parsedJSONB.array());
        assertEquals(position, buffer.position());
        assertEquals(limit, buffer.limit());
    }
}
