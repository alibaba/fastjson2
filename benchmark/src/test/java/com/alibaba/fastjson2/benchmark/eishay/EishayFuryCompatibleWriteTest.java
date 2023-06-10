package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayFuryCompatibleWriteTest {
    static final EishayFuryCompatibleWrite benchmark = new EishayFuryCompatibleWrite();
    static final int COUNT = 1_000_000;

    public static void jsonb() throws Exception {
        System.out.println("jsonb size " + benchmark.jsonbSize()); // 409
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryCompatibleWrite-jsonb millis : " + millis);
            // zulu8.68.0.21_AppleM1Max : 3839 3798 3763
            // zulu11.62.17_AppleM1Max : 3327 3244 3310
            // zulu17.40.19_AppleM1Max : 3287 3256 3321 3296 3257

            // jdk1.8.0_361_x86_i9 6372
            // jdk17.0.6_x86_i9 4674
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
            // zulu8.68.0.21_AppleM1Max : 2644 3018
            // zulu11.62.17_AppleM1Max : 2270 2510
            // zulu17.40.19_AppleM1Max : 2296 2319 2293

            // jdk1.8.0_361_x86_i9 5279
            // jdk17.0.6_x86_i9 5910
        }
    }

    public static void main(String[] args) throws Exception {
        jsonb();
//        fury();
    }
}
