package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayFuryCompatibleWriteTest {
    static final EishayFuryCompatibleWrite benchmark = new EishayFuryCompatibleWrite();

    public static void fastjson2JSONB() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2JSONB(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryWrite-fastjson2_jsonb millis : " + millis);
            // zulu8.62.0.19 : 415 385 402 386
            // zulu11.52.13 : 360 346 336 330 328 325
            // zulu17.38.21 : 367 345 339
        }
    }

    public static void fury() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fury(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryWrite-fury millis : " + millis);
            // zulu8.62.0.19 : 294
            // zulu11.52.13 : 272
            // zulu17.38.21 : 289
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2JSONB();
//        fury();
    }
}
