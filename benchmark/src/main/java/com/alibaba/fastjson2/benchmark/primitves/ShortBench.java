package com.alibaba.fastjson2.benchmark.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.openjdk.jmh.infra.Blackhole;

public class ShortBench {
    private static final short[] VALUES;

    static {
        short[] shorts = new short[Short.MAX_VALUE - Short.MIN_VALUE];
        for (int i = 0; i < shorts.length; i++) {
            shorts[i] = (short) (Short.MIN_VALUE + i);
        }
        VALUES = shorts;
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
        BH.consume(jsonWriter.getBytes());
        jsonWriter.close();
    }

    public void bean_jsonBytes(Blackhole BH) {
        for (int i = 0; i + 10 <= VALUES.length; i += 10) {
            Bean bean = new Bean();
            bean.v0 = VALUES[i];
            bean.v1 = VALUES[i + 1];
            bean.v2 = VALUES[i + 2];
            bean.v3 = VALUES[i + 3];
            bean.v4 = VALUES[i + 4];
            bean.v5 = VALUES[i + 5];
            bean.v6 = VALUES[i + 6];
            bean.v7 = VALUES[i + 7];
            bean.v8 = VALUES[i + 8];
            bean.v9 = VALUES[i + 9];
            byte[] bytes = JSON.toJSONBytes(bean);
            BH.consume(bytes);
        }
    }

    public void bean_jsonStr(Blackhole BH) {
        for (int i = 0; i + 10 <= VALUES.length; i += 10) {
            Bean bean = new Bean();
            bean.v0 = VALUES[i];
            bean.v1 = VALUES[i + 1];
            bean.v2 = VALUES[i + 2];
            bean.v3 = VALUES[i + 3];
            bean.v4 = VALUES[i + 4];
            bean.v5 = VALUES[i + 5];
            bean.v6 = VALUES[i + 6];
            bean.v7 = VALUES[i + 7];
            bean.v8 = VALUES[i + 8];
            bean.v9 = VALUES[i + 9];
            String jsonStr = JSON.toJSONString(bean);
            BH.consume(jsonStr);
        }
    }

    public void bean_jsonb(Blackhole BH) {
        for (int i = 0; i + 10 <= VALUES.length; i += 10) {
            Bean bean = new Bean();
            bean.v0 = VALUES[i];
            bean.v1 = VALUES[i + 1];
            bean.v2 = VALUES[i + 2];
            bean.v3 = VALUES[i + 3];
            bean.v4 = VALUES[i + 4];
            bean.v5 = VALUES[i + 5];
            bean.v6 = VALUES[i + 6];
            bean.v7 = VALUES[i + 7];
            bean.v8 = VALUES[i + 8];
            bean.v9 = VALUES[i + 9];
            byte[] bytes = JSONB.toBytes(bean);
            BH.consume(bytes);
        }
    }

    public static class Bean {
        public short v0;
        public short v1;
        public short v2;
        public short v3;
        public short v4;
        public short v5;
        public short v6;
        public short v7;
        public short v8;
        public short v9;
    }
}
