package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayFuryWriteTest {
    static final EishayFuryWrite benchmark = new EishayFuryWrite();

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

    public static void fury() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
//                benchmark.fury(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayWriteBinaryArrayMapping-fury millis : " + millis);
            // zulu8.62.0.19 : 271 314
            // zulu11.52.13 : 236
            // zulu17.32.13 : 283
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2JSONB();
//        fury();
//        kryo();
    }
}
