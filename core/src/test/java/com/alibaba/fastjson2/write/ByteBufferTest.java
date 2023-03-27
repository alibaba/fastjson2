package com.alibaba.fastjson2.write;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

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
}
