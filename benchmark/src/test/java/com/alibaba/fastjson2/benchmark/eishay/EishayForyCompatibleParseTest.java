package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayForyCompatibleParseTest {
    static final EishayForyCompatibleParse benchmark = new EishayForyCompatibleParse();
    static final int COUNT = 10_000_000;

    public static void jsonb() throws Exception {
        System.out.println("EishayForyParse-jsonb size " + benchmark.jsonbBytes.length); // 409
        System.out.println();

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayForyParse-fastjson2_jsonb millis : " + millis);
            // zulu8.68.0.21_AppleM1Max : 3824 3849 3678 3441 3315
            // zulu11.62.17_AppleM1Max : 3023 3196 3174 2888 3227
            // zulu17.40.19_AppleM1Max : 3109 3074 3140 2875 3004 2985 2808
            // oracle-jdk-17.0.6 3052

            // jdk1.8.0_361_x86_i9 7343
            // jdk17.0.6_x86_i9 6338
        }
    }

    public static void fory() {
        System.out.println("EishayForyParse-fory size " + benchmark.foryCompatibleBytes.length); // 661
        System.out.println();

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fory(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayForyParse-fory millis : " + millis);
            // zulu8.68.0.21_AppleM1Max : 2840 2719 2879
            // zulu11.62.17_AppleM1Max : 3361 3385 4396 2964
            // zulu17.40.19_AppleM1Max : 3360 3403 3490 3371

            // jdk1.8.0_361_x86_i9 4488
            // jdk17.0.6_x86_i9 4425
        }
    }

    public static void main(String[] args) throws Exception {
        jsonb();
//        fory();
    }
}
