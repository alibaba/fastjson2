package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class BigDecimalWriteTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final BigDecimalWrite benchmark = new BigDecimalWrite();
    static final int COUNT = 1_000_000;

    public static void fastjson2() throws Throwable {
        for (int j = 0; j < 5; j++) {
//            BigDecimalWrite.init();
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimalWrite-fastjson2 millis : " + millis);
            // zulu8.68.0.21 : 451
            // zulu11.62.17 : 373
            // zulu17.42.19 : 354
            // zulu20.28.85 : 388
        }
    }
    public static void jackson() throws Throwable {
        for (int j = 0; j < 5; j++) {
//            BigDecimalWrite.init();
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.jackson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimalWrite-jackson millis : " + millis);
            // zulu8.68.0.21 : 638
            // zulu11.62.17 : 453
            // zulu17.38.21 : 474
            // zulu20.28.85 : 479
        }
    }

    public static void fastjson2Plain() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjson2Plain(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimalWrite-fastjson2 millis : " + millis);
            // zulu8.68.0.21 : 1841
            // zulu11.62.17 : 1907
            // zulu17.42.19 : 1988
            // zulu20.28.85 : 1971
        }
    }

    public static void jacksonPlain() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.jacksonPlain(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimalWrite-jackson millis : " + millis);
            // zulu8.68.0.21 : 3814
            // zulu11.62.17 : 3894
            // zulu17.38.21 : 3013
            // zulu20.28.85 :
        }
    }

    public static void main(String[] args) throws Throwable {
        fastjson2();
//        fastjson2Plain();
//        jackson();
//        jacksonPlain();
    }
}
