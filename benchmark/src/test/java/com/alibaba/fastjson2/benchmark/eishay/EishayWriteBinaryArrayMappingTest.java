package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayWriteBinaryArrayMappingTest {
    static final EishayWriteBinaryArrayMapping benchmark = new EishayWriteBinaryArrayMapping();

    public static void kryo_test() throws Exception {
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
        // zulu8.58.0.13 : 395
        // zulu11.52.13 : 203
        // zulu17.32.13 :
    }

    public static void fastjson2JSONB_test() throws Exception {
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
        // zulu8.58.0.13 : 285
        // zulu11.52.13 :
        // zulu17.32.13 :
    }

    public static void ffastjson2JSONBArrayMapping_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            fastjson2JSONBArrayMapping();
        }
    }

    public static void fastjson2JSONBArrayMapping() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson2JSONB(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastjson2_jsonb millis : " + millis);
        // zulu8.58.0.13 : 188
        // zulu11.52.13 :
        // zulu17.32.13 :
    }

    public static void main(String[] args) throws Exception {
        fastjson2JSONB_test();
//        ffastjson2JSONBArrayMapping_test();
//        kryo_test();
    }
}
