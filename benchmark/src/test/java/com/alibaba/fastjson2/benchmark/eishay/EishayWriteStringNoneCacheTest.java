package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayWriteStringNoneCacheTest {
    static final int LOOP = 1000;
    static final EishayWriteStringNoneCache benchmark = new EishayWriteStringNoneCache();

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu8.58.0.13 : 612 598 591 556 533
            // zulu11.52.13 :
            // zulu17.32.13 :

            // reflect-zulu8.58.0.13 :
            // reflect-zulu11.52.13 :
            // reflect-zulu17.32.13 :
        }
    }

    public static void fastjson2Mixin() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2Mixin(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2Mixin millis : " + millis);
            // zulu8.58.0.13 : 612
            // zulu11.52.13 :
            // zulu17.32.13 :

            // reflect-zulu8.58.0.13 :
            // reflect-zulu11.52.13 :
            // reflect-zulu17.32.13 :
        }
    }

    public static void fastjson1() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson1(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson1 millis : " + millis);
            // zulu8.58.0.13 : 983
            // zulu11.52.13 :
            // zulu17.32.13 :

            // reflect-zulu8.58.0.13 :
            // reflect-zulu11.52.13 :
            // reflect-zulu17.32.13 :
        }
    }

    public static void jackson() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.jackson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("jackson millis : " + millis);
            // zulu8.58.0.13 : 42
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void gson() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.gson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("jackson millis : " + millis);
            // zulu8.58.0.13 : 40
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson1();
        fastjson2();
//        fastjson2Mixin();
//        jackson();
//        gson();
    }
}
