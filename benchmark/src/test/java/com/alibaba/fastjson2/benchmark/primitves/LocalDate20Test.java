package com.alibaba.fastjson2.benchmark.primitves;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class LocalDate20Test {
    public static void fastjson2_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_perf();
        }
    }

    public static void fastjson2_perf() {
        LocalDate20 benchmark = new LocalDate20();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson2(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("LocalDate20-fastjson2 : " + millis);

        // zulu8.62.0.19 : 624 642
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
        LocalDate20 benchmark = new LocalDate20();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson1(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("LocalDate20-fastjson1 : " + millis);
        // zulu8.62.0.19 : 6932
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
