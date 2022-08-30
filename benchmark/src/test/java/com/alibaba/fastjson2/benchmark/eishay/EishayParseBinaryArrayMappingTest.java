package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayParseBinaryArrayMappingTest {
    static final EishayParseBinaryArrayMapping benchmark = new EishayParseBinaryArrayMapping();

    public static void kryo_test() throws Exception {
        System.out.println("kryo size " + EishayParseBinaryArrayMapping.kryoBytes.length); // 214
        System.out.println();
        for (int i = 0; i < 10; i++) {
            kryo();
        }
    }

    public static void kryo() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.kryo(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("kryo millis : " + millis);
        // zulu8.58.0.13 : 457
        // zulu11.52.13 :
        // zulu17.32.13 :
    }

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
        // zulu8.58.0.13 :
        // zulu11.52.13 :
        // zulu17.32.13 : 166
    }

    public static void main(String[] args) throws Exception {
        fastjson2JSONB_test();
//        kryo_test();
    }
}
