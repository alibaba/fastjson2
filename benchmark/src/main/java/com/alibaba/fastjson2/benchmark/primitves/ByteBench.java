package com.alibaba.fastjson2.benchmark.primitves;

import com.alibaba.fastjson2.JSONWriter;
import org.openjdk.jmh.infra.Blackhole;

public class ByteBench {
    private static final byte[] VALUES;

    static {
        byte[] bytes = new byte[Byte.MAX_VALUE - Byte.MIN_VALUE];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (Byte.MIN_VALUE + i);
        }
        VALUES = bytes;
    }

    public void utf8(Blackhole BH) {
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        jsonWriter.writeAny(VALUES);
        BH.consume(jsonWriter.getBytes());
        jsonWriter.close();
    }

    public void utf16(Blackhole BH) {
        JSONWriter jsonWriter = JSONWriter.ofUTF16();
        jsonWriter.writeAny(VALUES);
        BH.consume(jsonWriter.toString());
        jsonWriter.close();
    }

    public void jsonb(Blackhole BH) {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeAny(VALUES);
        BH.consume(jsonWriter.getBytes());
        jsonWriter.close();
    }
}
