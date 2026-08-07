package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class LocalDateOfTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final LocalDateOf benchmark = new LocalDateOf();
    static final int COUNT = 100_000_000;

    public static void ofDate() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.ofDate(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("LocalDateOf-ofDate millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void ofDateTime() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.ofDateTime(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("LocalDateOf-ofDateTime millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void ofTime() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.ofTime(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("LocalDateOf-ofTime millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void main(String[] args) throws Throwable {
        ofTime();
//        ofDate();
//        ofDateTime();
    }
}
