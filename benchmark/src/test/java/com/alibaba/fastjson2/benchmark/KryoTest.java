package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSONB;
import com.caucho.hessian.io.Hessian2Output;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class KryoTest {
    Kryo kryo = new Kryo();
    Output output = new Output(1024, -1);
//
//    static io.fury.ThreadSafeFury fury = io.fury.Fury.builder()
//            .withLanguage(io.fury.Language.JAVA)
//            .withReferenceTracking(true)
//            .disableSecureMode()
//            .withCompatibleMode(io.fury.serializers.CompatibleMode.COMPATIBLE)
//            .buildThreadSafeFury();

    @Test
    public void test() throws IOException {
        for (int i = -128; i < 128; i++) {
            StringBuffer buf = new StringBuffer();
            buf.append("| ").append(i).append(" | ")
                    .append(toString(jsonb(i), false)).append(" | ")
                    .append(toString(kryo(i), false)).append(" | ")
                    .append(toString(hessian(i), false)).append(" | ");
//            buf.append(toString(fury(i), false)).append(" | ");
            System.out.println(buf);
        }
    }

    @Test
    public void testSize() throws IOException {
        long[] ranges = new long[8];

        for (int i = 1; i <= ranges.length; ++i) {
            int x = -1;
            int min = 0, max = Integer.MAX_VALUE;
            for (; ; ) {
                int delta = max - min;
                int mid = min + delta / 2;
                int size = jsonb(mid).length;
                if (size > i) {
                    max = mid;
                } else if (size == i) {
                    if (min == mid) {
                        if (jsonb(max).length == i) {
                            x = max;
                        } else {
                            x = min;
                        }
                        break;
                    }
                    min = mid;
                } else {
                    break;
                }
            }
            ranges[i - 1] = x;
        }
        System.out.println(Arrays.toString(ranges));
    }

    public byte[] kryo(Object o) {
        output.reset();
        kryo.writeObject(output, o);
        byte[] bytes = output.toBytes();
        return bytes;
    }
//
//    public byte[] fury(Object o) {
//        return fury.serialize(o);
//    }

    public byte[] jsonb(Object o) {
        return JSONB.toBytes(o);
    }

    public byte[] hessian(Object o) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
        hessian2Output.writeObject(o);
        hessian2Output.flush();
        return byteArrayOutputStream.toByteArray();
    }

    String toString(byte[] bytes, boolean quote) {
        StringBuffer buf = new StringBuffer();
        if (quote) {
            buf.append("[");
        }
        for (int i = 0; i < bytes.length; i++) {
            if (i != 0) {
                buf.append(' ');
            }

            buf.append("0x");
            int b = bytes[i] & 0xFF;
            if (b < 16) {
                buf.append('0');
            }
            buf.append(Integer.toString(b, 16));
        }
        if (quote) {
            buf.append("]");
        }
        return buf.toString();
    }
}
