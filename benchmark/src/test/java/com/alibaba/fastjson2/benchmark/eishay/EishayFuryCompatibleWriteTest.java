package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayFuryCompatibleWriteTest {
    static final EishayFuryCompatibleWrite benchmark = new EishayFuryCompatibleWrite();
    static final int COUNT = 10_000_000;

    public static void jsonb() throws Exception {
        System.out.println("jsonb size " + benchmark.jsonbSize()); // 409
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryCompatibleWrite-jsonb millis : " + millis);
            // zulu8.68.0.21_AppleM1Max : 3839
            // zulu11.62.17_AppleM1Max : 3327
            // zulu17.40.19_AppleM1Max : 3287
        }
    }

    public static void fury() throws Exception {
        System.out.println("fury size " + benchmark.furySize()); // 661
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fury(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryCompatibleWrite-fury millis : " + millis);
            // zulu8.68.0.21_AppleM1Max : 2644
            // zulu11.62.17_AppleM1Max : 2270
            // zulu17.40.19_AppleM1Max : 2296
        }
    }

    public static void main(String[] args) throws Exception {
        jsonb();
//        fury();
    }
}
