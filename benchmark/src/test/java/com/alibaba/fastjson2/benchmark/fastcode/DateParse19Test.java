package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class DateParse19Test {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final DateParse19 benchmark = new DateParse19();
    static final int COUNT = 100_000_000;

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
            // zulu8.58.0.13 : 3843 1798 1741
            // zulu11.52.13 : 2564 2137 1706
            // zulu17.38.21 : 1868 1880 1708
        }
    }

    public static void main(String[] args) throws Throwable {
//        parseDate();
//        parseDateSmart();
//        javaTimeDateTimeFormatter();
//        javaTimeDateTimeFormatter1();
    }
}
