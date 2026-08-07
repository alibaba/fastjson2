package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class FloatToBigDecimalTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final FloatToBigDecimal benchmark = new FloatToBigDecimal();
    static final int COUNT = 10_000_000;

    public static void fastjson2() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("FloatToBigDecimal-fastjson2 millis : " + millis);
            // zulu8.68.0.21 : 425
            // zulu11.62.17 : 473
            // zulu17.40.19 : 438
            // graalvm-ce-17-22.3.1 : 391
            // graalvm-ee-17-22.3.1 : 319
        }
    }

    public static void jdk() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.jdk(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("FloatToBigDecimal-jdk millis : " + millis);
            // zulu8.68.0.21 : 863
            // zulu11.62.17 : 874
            // zulu17.40.19 : 869
            // graalvm-ce-17-22.3.1 : 910
            // graalvm-ee-17-22.3.1 : 743
        }
    }

    public static void main(String[] args) throws Throwable {
//        fastjson2();
        jdk();
    }
}
