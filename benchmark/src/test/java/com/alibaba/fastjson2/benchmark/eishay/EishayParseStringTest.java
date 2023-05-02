package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayParseStringTest {
    static final int LOOP = 10_000_000;
    static final EishayParseString benchmark = new EishayParseString();

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu8.70.0.23 : 5569
            // zulu11.64.19 : 5022
            // zulu17.42.19 : 5377
        }
    }

    public static void fastjson2Mixin() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2Mixin(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2Mixin millis : " + millis);
            // zulu8.70.0.23 : 6432
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void fastjson1() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson1(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson1 millis : " + millis);
            // zulu8.70.0.23 : 6436
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void jackson() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.jackson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("jackson millis : " + millis);
            // zulu8.70.0.23 : 13191
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void wastjson() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.wastjson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseString-wastjson millis : " + millis);
            // zulu8.70.0.23 : 5458
            // zulu11.64.19 : 4574
            // zulu17.42.19 : 4515
        }
    }

    public static void gson() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.gson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseString-gson millis : " + millis);
            // zulu8.70.0.23 : 15017
            // zulu11.64.19 : 13143
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2();
//        fastjson2Mixin();
//        jackson();
//        fastjson1();
//        gson();
        wastjson();
    }
}
