package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayParseBinaryTest {
    static final EishayParseBinary benchmark = new EishayParseBinary();

    public static void kryo_test() throws Exception {
        System.out.println("kryo size " + EishayParseBinary.kryoBytes.length); // 214
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
        System.out.println("fastjson2_jsonb size " + EishayParseBinary.fastjson2JSONBBytes.length); // 348
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
        // zulu8.58.0.13 : 333
        // zulu11.52.13 :
        // zulu17.32.13 :
    }

    public static void fastjson2JSONBArrayMapping_test() throws Exception {
        System.out.println("fastjson2_jsonb_arrayMapping size " + EishayParseBinary.fastjson2JSONBBytes_ArrayMapping.length); // 223
        System.out.println();

        for (int i = 0; i < 10; i++) {
            fastjson2JSONBArrayMapping();
        }
    }

    public static void fastjson2JSONBArrayMapping() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson2JSONBArrayMapping(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastjson2_jsonb millis : " + millis);
        // zulu8.58.0.13 : 197
        // zulu11.52.13 :
        // zulu17.32.13 :
    }

    public static void main(String[] args) throws Exception {
//        fastjson2JSONB_test();
        fastjson2JSONBArrayMapping_test();
//        kryo_test();
    }
}
