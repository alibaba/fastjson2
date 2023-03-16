package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class BigDecimalNewTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final BigDecimalNew benchmark = new BigDecimalNew();
    static final int COUNT = 1_000_000;

    public static void string() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.string(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat10-string millis : " + millis);
            // zulu8.58.0.13 : 3874
            // zulu11.52.13 :
            // zulu17.38.21 :
            // zulu21.0.35 : 2739
        }
    }

    public static void string1() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.string1(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat10-string1 millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.38.21 :
            // zulu21.0.35 : 2307
        }
    }

    public static void string2() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.string2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat10-string1 millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.38.21 :
            // zulu21.0.35 : 1367
        }
    }

    public static void chars() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.chars(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat10-chars millis : " + millis);
            // zulu8.58.0.13 : 2201
            // zulu11.52.13 :
            // zulu17.40.19 : 2185
        }
    }

    public static void chars2() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.chars2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat10-chars2 millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.40.19 : 2005
        }
    }

    public static void bytes() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.bytes(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat10-bytes millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.40.19 : 1191
            // zulu21.0.35 : 1248
        }
    }

    public static void main(String[] args) throws Throwable {
//        string();
//        string1();
        string2();
//        chars();
//        chars2();
//        bytes();
    }
}
