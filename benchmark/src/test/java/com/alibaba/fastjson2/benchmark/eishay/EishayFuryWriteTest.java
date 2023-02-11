package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayFuryWriteTest {
    static final EishayFuryWrite benchmark = new EishayFuryWrite();
    static final int COUNT = 10_000_000;

    public static void jsonb() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryWrite-jsonb millis : " + millis);
            // zulu8.62.0.19 : 2007
            // zulu11.52.13 : 1483
            // zulu17.38.21 : 1496
            // oracle-jdk-17.0.6.jdk 1523
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
            // oracle-jdk-17.0.6.jdk 1659
        }
    }

    public static void main(String[] args) throws Exception {
        jsonb();
//        fury();
    }
}
