package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class DateWriteTest {
    static final DateWrite benchmark = new DateWrite();
    static final int COUNT = 1_000_000;

    public static void dateTimeFormatter() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.dateTimeFormatter(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateWrite-dateTimeFormatter : " + millis);

            // zulu8.62.0.19 :
            // zulu11.52.13 :
            // zulu17.32.13 :
            // zulu18.28.13 : 1314
            // zulu19.0.47 :
            // corretto-8 :
            // corretto-11 :
            // corretto-17 :
            // corretto-18 :
            // oracle-jdk-17.0.4 :
            // oracle-jdk-18.0.2 :
        }
    }

    public static void simpleDateFormat() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.simpleDateFormat(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateWrite-simpleDateFormat : " + millis);

            // zulu8.62.0.19 :
            // zulu11.52.13 :
            // zulu17.32.13 :
            // zulu18.28.13 :
            // zulu19.0.47 :
            // corretto-8 :
            // corretto-11 :
            // corretto-17 :
            // corretto-18 :
            // oracle-jdk-17.0.4 :
            // oracle-jdk-18.0.2 :
        }
    }

    public static void formatYYYYMMDDHHMMSS19() throws Throwable {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.formatYYYYMMDDHHMMSS19(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateWrite-formatYYYYMMDDHHMMSS19 : " + millis);

            // zulu8.62.0.19 : 314
            // zulu11.52.13 : 306
            // zulu17.32.13 : 379
            // zulu18.28.13 :
            // zulu19.0.47 :
            // corretto-8 :
            // corretto-11 :
            // corretto-17 :
            // corretto-18 : 380
            // oracle-jdk-17.0.4 :
            // oracle-jdk-18.0.2 :
        }
    }

    public static void main(String[] args) throws Throwable {
//        dateTimeFormatter();
        formatYYYYMMDDHHMMSS19();
    }
}
