package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class DateParseMultiFormatTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final DateParseMultiFormat benchmark = new DateParseMultiFormat();
    static final int COUNT = 1_000_000;

    public static void javaTimeFormatter() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.javaTimeFormatter(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateParseMultiFormat-javaTimeFormatter millis : " + millis);
            // zulu8.58.0.13 : 1395
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void parseDate() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.parseDate(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateParseMultiFormat-parseDate millis : " + millis);
            // zulu8.58.0.13 : 71
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void main(String[] args) throws Throwable {
//        javaTimeFormatter();
        parseDate();
    }
}
