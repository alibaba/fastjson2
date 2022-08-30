package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class DateParseTest {
    static final DateParse benchmark = new DateParse();
    static final int COUNT = 1_00_000;

    public static void dateTimeFormatterParse_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            dateTimeFormatterParse();
        }
    }

    public static void dateTimeFormatterParse() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; ++i) {
            benchmark.dateTimeFormatter(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("CartTree-fastjson2 : " + millis);

        // zulu8.62.0.19 : 343
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

    public static void dateTimeFormatterParse2_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            dateTimeFormatterParse2();
        }
    }

    public static void dateTimeFormatterParse2() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; ++i) {
            benchmark.localDateTimeParse(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("CartTree-fastjson2 : " + millis);

        // zulu8.62.0.19 : 332
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

    public static void parseYYYYMMDDHHMMSS19_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            parseYYYYMMDDHHMMSS19();
        }
    }

    public static void parseYYYYMMDDHHMMSS19() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; ++i) {
            benchmark.parseYYYYMMDDHHMMSS19(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("CartTree-fastjson2 : " + millis);

        // zulu8.62.0.19 : 67
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
//
//    @Test
//    public void testDates() {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        format.setTimeZone(TimeZone.getTimeZone(DateParse.SHANGHAI_ZONE_ID));
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//        char[] chars = "\"1900-01-01 00:00:00\"".toCharArray();
//        for (int year = 1900; year < 2200; year++) {
//            IOUtils.getChars(year, 5, chars);
//
//            for (int month = 1; month <= 12; month++) {
//                chars[6] = '0';
//                IOUtils.getChars(month, 8, chars);
//
//                int dom = 31;
//                switch (month) {
//                    case 2:
//                        dom = (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
//                        break;
//                    case 4:
//                    case 6:
//                    case 9:
//                    case 11:
//                        dom = 30;
//                        break;
//                }
//
//                for (int d = 1; d <= dom; d++) {
//                    chars[9] = '0';
//                    IOUtils.getChars(d, 11, chars);
//
//                    for (int h = 1; h <= 12; h++) {
//                        chars[12] = '0';
//                        IOUtils.getChars(h, 14, chars);
//                        String str1 = new String(chars, 1, 19);
//
//                        long millis19 = DateParse.parseYYYYMMDDHHMMSS19(str1).getTime();
//
//                        LocalDateTime ldt = LocalDateTime.parse(str1, formatter);
//                        ZonedDateTime zdt = ZonedDateTime.ofLocal(ldt, DateParse.SHANGHAI_ZONE_ID, null);
//                        assertEquals(zdt.toInstant().toEpochMilli(), millis19);
//                    }
//                }
//            }
//        }
//    }

    public static void main(String[] args) throws Exception {
        DateParse.parseYYYYMMDDHHMMSS19("2012-06-23 01:02:03");
        dateTimeFormatterParse2_test();
//        parseYYYYMMDDHHMMSS19_test();
    }
}
