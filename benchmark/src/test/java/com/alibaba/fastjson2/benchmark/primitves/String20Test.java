package com.alibaba.fastjson2.benchmark.primitves;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class String20Test {
    static final String20 benchmark = new String20();

    public static void fastjson2_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2();
        }
    }

    public static void fastjson2() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson2(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("String20-fastjson2 : " + millis);
        // zulu8.62.0.19 : 598 566 548
        // zulu11.52.13 : 475 440 431
        // zulu17.32.13 :
        // corretto-8 : 532
    }

    public static void fastjson2_jsonb_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_jsonb();
        }
    }

    public static void fastjson2_jsonb() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson2_jsonb(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("String20-fastjson2_jsonb : " + millis);
        // zulu8.62.0.19 : 312
        // zulu11.52.13 : 258
        // zulu17.32.13 : 357 260
        // corretto-8 :
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
        System.out.println("String20-jackson : " + millis);
        // zulu8.62.0.19 : 1088
        // zulu11.52.13 :
        // zulu17.32.13 :
        // corretto-8 : 965
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
        System.out.println("String20-wastjson : " + millis);
        // zulu8.62.0.19 : 524
        // zulu11.52.13 : 343
        // zulu17.32.13 :
        // corretto-8 :
    }

    public static void main(String[] args) throws Exception {
        fastjson2_test();
//        fastjson2_jsonb_test();
//        jackson_test();
//        wastjson_test();
    }
}
