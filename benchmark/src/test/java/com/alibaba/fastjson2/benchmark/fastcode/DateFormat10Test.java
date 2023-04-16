package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class DateFormat10Test {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final DateFormat10 benchmark = new DateFormat10();
    static final int COUNT = 100_000_000;

    public static void fastjson_format() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjson_format(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat10-fastjsonFormat millis : " + millis);
            // zulu8.58.0.13 : 776
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void fastjson_formatYMD10() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjson_formatYMD10(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat10-fastjson_formatYMD10 millis : " + millis);
            // zulu8.58.0.13 : 553
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void simpleFormat() throws Throwable {
        final int COUNT = 1_000_000;
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.simpleFormat(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat10-simpleFormat millis : " + millis);
            // zulu8.58.0.13 : 586
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void simpleFormatX() throws Throwable {
        final int COUNT = 1_000_000;
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.simpleFormatX(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat10-simpleFormatX millis : " + millis);
            // zulu8.58.0.13 : 444
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void simpleParse() throws Throwable {
        final int COUNT = 1_000_000;
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.simpleParse(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat10-simpleParse millis : " + millis);
            // zulu8.58.0.13 : 651
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void simpleParseX() throws Throwable {
        final int COUNT = 1_000_000;
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.simpleParseX(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat10-simpleParseX millis : " + millis);
            // zulu8.58.0.13 : 368
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void main(String[] args) throws Throwable {
//        fastjson_format();
//        fastjson_formatYMD10();
//        fastjsonFormat2();
//        simpleFormat();
//        simpleFormatX();
        simpleParse();
//        simpleParseX();
    }
}
