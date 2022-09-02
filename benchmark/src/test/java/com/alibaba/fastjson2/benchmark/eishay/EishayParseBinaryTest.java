package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayParseBinaryTest {
    static final EishayParseBinary benchmark = new EishayParseBinary();

    public static void fastjson2JSONB_test() throws Exception {
        System.out.println("fastjson2_jsonb size " + EishayParseBinaryArrayMapping.fastjson2JSONBBytes.length); // 348
        System.out.println();

        for (int i = 0; i < 10; i++) {
            fastjson2JSONB();
        }
    }

    public static void fastjson2JSONB() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson2JSONB(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastjson2_jsonb millis : " + millis);
        // zulu8.58.0.13 : 334 331
        // zulu11.52.13 :
        // zulu17.32.13 : 314 310 301
    }

    public static void main(String[] args) throws Exception {
        fastjson2JSONB_test();
//        kryo_test();
    }
}
