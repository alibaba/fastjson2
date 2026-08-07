package com.alibaba.fastjson2.benchmark.utf8;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class UTF8EncodeTest {
    static final UTF8Encode benchmark = new UTF8Encode();

    public static void jdk() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.jdk(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimal20-fastjson2 : " + millis);

            // zulu8.68.0.21 : 334
            // zulu11.52.13 : 303
            // zulu17.32.13 : 306
        }
    }

    public static void fj() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fj(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimal20-fastjson2 : " + millis);

            // zulu8.68.0.21 : 329
            // zulu11.52.13 :
            // zulu17.32.13 : 327
        }
    }

    public static void jdk_small() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000 * 10; ++i) {
                benchmark.jdk_small(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimal20-fastjson2 : " + millis);

            // zulu8.68.0.21 : 217
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void fj_small() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000 * 10; ++i) {
                benchmark.fj_small(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimal20-fastjson2 : " + millis);

            // zulu8.68.0.21 : 233
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
//        jdk();
//        fj();
//        jdk_small();
        fj_small();
    }
}
