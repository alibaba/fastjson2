package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayFuryWriteTest {
    static final EishayFuryWrite benchmark = new EishayFuryWrite();
    static final int COUNT = 10_000_000;

    public static void fastjson2JSONB() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjson2JSONB(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryWrite-fastjson2_jsonb millis : " + millis);
            // zulu8.62.0.19 : 2007
            // zulu11.52.13 : 1483
            // zulu17.38.21 : 1496
        }
    }

    public static void fury() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fury(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryWrite-fury millis : " + millis);
            // zulu8.62.0.19 : 2208
            // zulu11.52.13 : 1740
            // zulu17.38.21 : 1710
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2JSONB();
//        fury();
    }
}
