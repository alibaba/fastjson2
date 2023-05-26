package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayFuryParseTest {
    static final EishayFuryParse benchmark = new EishayFuryParse();
    static final int COUNT = 10_000_000;

    public static void jsonb() throws Exception {
        System.out.println("EishayFuryParse-jsonb size " + benchmark.jsonbBytes.length); // 282
        System.out.println();

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryParse-jsonb millis : " + millis);
            // zulu8.62.0.19 : 2513 2541
            // zulu11.52.13 : 2352
            // zulu17.38.21 : 2336
        }
    }

    public static void fury() throws Exception {
        System.out.println("EishayFuryParse-fury size " + benchmark.furyBytes.length); // 410
        System.out.println();

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fury(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryParse-fury millis : " + millis);
            // zulu8.62.0.19 : 2642
            // zulu11.52.13 : 2862
            // zulu17.38.21 : 2958
        }
    }

    public static void main(String[] args) throws Exception {
        jsonb();
//        fury();
    }
}
