package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayFuryCompatibleParseTest {
    static final EishayFuryCompatibleParse benchmark = new EishayFuryCompatibleParse();
    static final int COUNT = 10_000_000;

    public static void jsonb() throws Exception {
        System.out.println("EishayFuryParse-jsonb size " + benchmark.jsonbBytes.length); // 409
        System.out.println();

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryParse-fastjson2_jsonb millis : " + millis);
            // zulu8.68.0.21_AppleM1Max : 3824 3849
            // zulu11.62.17_AppleM1Max : 3023 3196
            // zulu17.40.19_AppleM1Max : 3109 3074
            // oracle-jdk-17.0.6 3052

            // jdk1.8.0_361_x86_i9 7343
            // jdk17.0.6_x86_i9 6338
        }
    }

    public static void fury() {
        System.out.println("EishayFuryParse-fury size " + benchmark.furyCompatibleBytes.length); // 661
        System.out.println();

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fury(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryParse-fury millis : " + millis);
            // zulu8.68.0.21_AppleM1Max : 2840
            // zulu11.62.17_AppleM1Max : 3361 3385
            // zulu17.40.19_AppleM1Max : 3360 3403

            // jdk1.8.0_361_x86_i9 4488
            // jdk17.0.6_x86_i9 4425
        }
    }

    public static void main(String[] args) throws Exception {
//        jsonb();
        fury();
    }
}
