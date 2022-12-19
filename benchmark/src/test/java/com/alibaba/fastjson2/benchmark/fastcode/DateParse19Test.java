package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class DateParse19Test {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final DateParse19 benchmark = new DateParse19();
    static final int COUNT = 100_000_000;

    public static void parseDateYMDHMS19() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.parseDateYMDHMS19(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateParse19-parseDateYMDHMS19 millis : " + millis);
            // zulu8.58.0.13 : 3306 1701 1661 1644 1714 1689
            // zulu11.52.13 : 2069 1971 1915 1643
            // zulu17.38.21 : 2891 2611 1948 1919 1669
        }
    }

    public static void parseDate() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.parseDate(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateParse19-parseDate millis : " + millis);
            // zulu8.58.0.13 : 2818 2041 1852 1824
            // zulu11.52.13 : 1966 1857
            // zulu17.38.21 : 1954 1894
        }
    }

    public static void parseDateSmart() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.parseDateSmart(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateParse19-parseDate millis : " + millis);
            // zulu8.58.0.13 : 3843 1798
            // zulu11.52.13 : 2564 2137
            // zulu17.38.21 : 1868 1880
        }
    }

    public static void javaTimeDateTimeFormatter() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT / 10; ++i) {
                benchmark.javaTimeDateTimeFormatter(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateParse19-javaTimeDateTimeFormatter millis : " + millis);
            // zulu8.58.0.13 : 3018
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void javaTimeDateTimeFormatter1() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT / 10; ++i) {
                benchmark.javaTimeDateTimeFormatter1(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateParse19-javaTimeDateTimeFormatter1 millis : " + millis);
            // zulu8.58.0.13 : 2785
            // zulu11.52.13 :
            // zulu17.38.21 :
        }
    }

    public static void main(String[] args) throws Throwable {
        parseDateYMDHMS19();
//        parseDate();
//        parseDateSmart();
//        javaTimeDateTimeFormatter();
//        javaTimeDateTimeFormatter1();
    }
}
