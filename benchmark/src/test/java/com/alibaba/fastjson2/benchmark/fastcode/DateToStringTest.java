package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class DateToStringTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final DateToString benchmark = new DateToString();
    static final int COUNT = 100_000_000;

    public static void dateToString() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.dateToString(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateToString-dateToString millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.38.21 :
            // jdk22-ea : 2495
            // jdk22-baseline : 5669
        }
    }

    public static void main(String[] args) throws Throwable {
        dateToString();
    }
}
