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
        // zulu17.32.13 :
        // zulu11.52.13 :
        // zulu8.62.0.19 : 553
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

    public static void jackson_perf_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            jackson_perf();
        }
    }

    public static void jackson_perf() throws Exception {
        String20 benchmark = new String20();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.jackson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("String20-jackson : " + millis);
        // zulu17.32.13 :
        // zulu11.52.13 :
        // zulu8.62.0.19 : 1088
        // corretto-8 : 965
    }

    public static void main(String[] args) throws Exception {
//        fastjson2_test();
        fastjson2_jsonb_test();
//        jackson_perf_test();
    }
}
