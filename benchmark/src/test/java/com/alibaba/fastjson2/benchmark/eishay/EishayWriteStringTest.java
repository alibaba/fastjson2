package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayWriteStringTest {
    static final EishayWriteString benchmark = new EishayWriteString();
    static final int LOOP = 1_000_000;

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu8.58.0.13 : 325 344 342
            // zulu11.52.13 : 347 369 344 353
            // zulu17.32.13 : 335 342 353

            // reflect-zulu8.58.0.13 : 498 465
            // reflect-zulu11.52.13 : 532 504
            // reflect-zulu17.32.13 : 508 486
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
            // zulu8.58.0.13 : 361
            // zulu11.52.13 : 435 434
            // zulu17.32.13 : 368 362

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
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void wastjson() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.wastjson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("wastjson millis : " + millis);
            // zulu8.58.0.13 : 467
            // zulu17.32.13 : 542
            // zulu11.52.13 :
        }
    }

    public static void gson() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.gson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("jackson millis : " + millis);
            // zulu8.58.0.13 : 1455
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2();
//        fastjson2Mixin();
//        jackson();
//        wastjson();
    }
}
