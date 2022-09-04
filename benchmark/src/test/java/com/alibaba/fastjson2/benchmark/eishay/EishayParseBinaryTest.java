package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayParseBinaryTest {
    static final EishayParseBinary benchmark = new EishayParseBinary();

    public static void fastjson2JSONB() throws Exception {
        System.out.println("fastjson2_jsonb size " + EishayParseBinaryArrayMapping.fastjson2JSONBBytes.length); // 348
        System.out.println();

        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2JSONB(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseBinary-fastjson2JSONB millis : " + millis);
            // zulu8.58.0.13 : 305
            // zulu11.52.13 : 249
            // zulu17.32.13 : 245

            // reflect-zulu8.58.0.13 : 389 386
            // reflect-zulu11.52.13 : 336
            // reflect-zulu11.52.13 : 325
        }
    }

    public static void kryo() throws Exception {
        System.out.println("fastjson2_jsonb size " + EishayParseBinaryArrayMapping.fastjson2JSONBBytes.length); // 348
        System.out.println();

        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.kryo(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseBinary-kryo millis : " + millis);
            // zulu8.58.0.13 : 462
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2JSONB();
//        kryo();
    }
}
