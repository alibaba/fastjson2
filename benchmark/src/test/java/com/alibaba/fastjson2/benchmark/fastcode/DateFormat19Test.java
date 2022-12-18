package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class DateFormat19Test {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final DateFormat19 benchmark = new DateFormat19();
    static final int COUNT = 10_000_000;

    public static void fastjsonFormat() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjsonFormat(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat19-fastjsonFormat millis : " + millis);
            // zulu8.58.0.13 : 4065
            // zulu11.52.13 :
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
            // zulu8.58.0.13 : 359
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void javaTimeDateFormatter() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.javaTimeDateFormatter(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateFormat19-javaTimeDateFormatter millis : " + millis);
            // zulu8.58.0.13 : 2178
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void main(String[] args) throws Throwable {
        fastjsonFormat();
//        fastjsonFormat2();
//        javaTimeDateFormatter();
    }
}
