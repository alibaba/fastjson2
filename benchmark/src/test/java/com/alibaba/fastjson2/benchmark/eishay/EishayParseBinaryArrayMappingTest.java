package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayParseBinaryArrayMappingTest {
    static final EishayParseBinaryArrayMapping benchmark = new EishayParseBinaryArrayMapping();

    public static void kryo() throws Exception {
        System.out.println("kryo size " + EishayParseBinaryArrayMapping.kryoBytes.length); // 214
        System.out.println();
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.kryo(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseBinaryArrayMapping-kryo millis : " + millis);
            // zulu8.58.0.13 : 457
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void fastjson2JSONB() throws Exception {
        System.out.println("EishayParseBinaryArrayMapping-fastjson2_jsonb size " + EishayParseBinaryArrayMapping.fastjson2JSONBBytes.length); // 348
        System.out.println();

        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2JSONB(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseBinaryArrayMapping-fastjson2_jsonb millis : " + millis);
            // zulu8.58.0.13 : 193 188
            // zulu11.52.13 : 160
            // zulu17.32.13 : 166 148 139
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2JSONB();
//        kryo();
//        fury();
    }
}
