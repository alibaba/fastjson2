package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayWriteBinaryArrayMappingTest {
    static final EishayWriteBinaryArrayMapping benchmark = new EishayWriteBinaryArrayMapping();

    public static void kryo() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.kryo(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayWriteBinaryArrayMapping-kryo millis : " + millis);
            // zulu8.62.0.19 : 396
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void fastjson2JSONB() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2JSONB(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayWriteBinaryArrayMapping-fastjson2_jsonb millis : " + millis);
            // zulu8.62.0.19 : 190 168
            // zulu11.52.13 : 105
            // zulu17.32.13 : 105
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2JSONB();
//        fury();
//        kryo();
    }
}
