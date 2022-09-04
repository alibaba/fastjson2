package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayWriteStringTest {
    static final EishayWriteString benchmark = new EishayWriteString();

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu8.58.0.13 : 325
            // zulu11.52.13 : 347 369
            // zulu17.32.13 : 335

            // reflect-zulu8.58.0.13 : 498
            // reflect-zulu11.52.13 : 532
            // reflect-zulu17.32.13 : 508
        }
    }

    public static void jackson_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            jackson();
        }
    }

    public static void jackson() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.jackson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("jackson millis : " + millis);
        // zulu8.58.0.13 :
        // zulu11.52.13 :
        // zulu17.32.13 :
    }

    public static void wastjson_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            wastjson();
        }
    }

    public static void wastjson() throws Exception {
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

    public static void gson_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            gson();
        }
    }

    public static void gson() throws Exception {
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

    public static void main(String[] args) throws Exception {
        fastjson2();
//        jackson_test();
//        wastjson_test();
    }
}
