package com.alibaba.fastjson2.benchmark.primitves;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class Instant20Test {
    public static void fastjson2_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_perf();
        }
    }

    public static void fastjson2_perf() {
        Instant20 benchmark = new Instant20();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson2(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("Instant20-fastjson2 : " + millis);

        // zulu8.62.0.19 : 1242 1181
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

    public static void fastjson1_perf_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            fastjson1_perf();
        }
    }

    public static void fastjson1_perf() throws Exception {
        Instant20 benchmark = new Instant20();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson1(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("Instant20-fastjson1 : " + millis);
        // zulu8.62.0.19 :
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

    public static void main(String[] args) throws Exception {
        fastjson2_perf_test();
//        fastjson1_perf_test();
    }
}
