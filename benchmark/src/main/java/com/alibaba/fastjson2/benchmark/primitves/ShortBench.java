package com.alibaba.fastjson2.benchmark.primitves;

import com.alibaba.fastjson2.JSONWriter;
import org.openjdk.jmh.infra.Blackhole;

public class ShortBench {
    private static final short[] SHORTS;

    static {
        short[] shorts = new short[Short.MAX_VALUE - Short.MIN_VALUE];
        for (int i = 0; i < shorts.length; i++) {
            shorts[i] = (short) (Short.MIN_VALUE + i);
        }
        SHORTS = shorts;
    }

    public void utf8(Blackhole BH) {
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        jsonWriter.writeAny(SHORTS);
        BH.consume(jsonWriter.getBytes());
        jsonWriter.close();
    }

    public void utf16(Blackhole BH) {
        JSONWriter jsonWriter = JSONWriter.ofUTF16();
        jsonWriter.writeAny(SHORTS);
        BH.consume(jsonWriter.getBytes());
        jsonWriter.close();
    }
}
