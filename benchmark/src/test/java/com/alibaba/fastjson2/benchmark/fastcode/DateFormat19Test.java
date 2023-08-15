package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class DateFormat19Test {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final DateFormat19 benchmark = new DateFormat19();
    static final int COUNT = 100_000_000;

    public static void fastjsonFormat() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjsonFormat(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat19-fastjsonFormat millis : " + millis);
            // zulu8.58.0.13 : 3112
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void formatYMDHMS19() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.formatYMDHMS19(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat19-formatYMDHMS19 millis : " + millis);
            // zulu8.58.0.13 : 3161 3589
            // zulu11.52.13 : 3410 3623 1589 1559
            // zulu17.38.21 :
        }
    }

    public static void fastjsonFormat2() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjsonFormat2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat19-fastjsonFormat2 millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 : 3501 2386 2078
            // zulu17.38.21 :
        }
    }

    public static void javaTimeDateFormatter() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.javaTimeFormatter(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat19-javaTimeDateFormatter millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void simpleParseX() throws Throwable {
        int COUNT = 1_000_000;
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.simpleParseX(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat19-javaTimeDateFormatter millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void main(String[] args) throws Throwable {
//        fastjsonFormat();
//        formatYMDHMS19();
        fastjsonFormat2();
//        javaTimeDateFormatter();
//        simpleParseX();
    }
}
