package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayFuryCompatibleWriteTest {
    static final EishayFuryCompatibleWrite benchmark = new EishayFuryCompatibleWrite();
    static final int COUNT = 10_000_000;

    public static void fastjson2JSONB() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjson2JSONB(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryCompatibleWrite-fastjson2_jsonb millis : " + millis);
            // zulu8.62.0.19 : 3850
            // zulu11.52.13 : 3229
            // zulu17.38.21 : 3295
        }
    }

    public static void fury() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fury(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryCompatibleWrite-fury millis : " + millis);
            // zulu8.62.0.19 : 2523
            // zulu11.52.13 : 2270
            // zulu17.38.21 : 3049
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2JSONB();
//        fury();
    }
}
