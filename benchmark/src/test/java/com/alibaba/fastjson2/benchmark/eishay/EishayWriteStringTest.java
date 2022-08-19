package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayWriteStringTest {
    static final EishayWriteString benchmark = new EishayWriteString();

    public static void fastjson2_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_perf();
        }
    }

    public static void fastjson2_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson2(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastjson2 millis : " + millis);
        // zulu8.58.0.13 : 325
        // zulu11.52.13 : 347 369
        // zulu17.32.13 :
    }

    public static void jackson_perf_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            jackson_perf();
        }
    }

    public static void jackson_perf() throws Exception {
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

    public static void wastjson_perf_test() throws Exception {
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
        System.out.println("jackson millis : " + millis);
        // zulu17.32.13 :
        // zulu11.52.13 :
        // zulu8.58.0.13 : 467
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
//        fastjson2_perf_test();
//        jackson_perf_test();
//        wastjson_perf_test();
    }
}
