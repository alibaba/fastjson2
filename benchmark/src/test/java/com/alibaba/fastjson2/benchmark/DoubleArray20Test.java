package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class DoubleArray20Test {
    public static void fastjson2_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_perf();
        }
    }

    public static void fastjson2_perf() {
        DoubleArray20 benchmark = new DoubleArray20();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson2(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("DoubleArray20-fastjson2 : " + millis);

        // zulu8.62.0.19 : 4182 2801
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

    public static void fastjson2_tree_perf() {
        DoubleArray20 benchmark = new DoubleArray20();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson2_tree(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("DoubleArray20-fastjson2 : " + millis);

        // zulu8.62.0.19 : 3716 3432 3296
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

    public static void fastjson2_tree_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_tree_perf();
        }
    }

    public static void jackson_perf_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            jackson_perf();
        }
    }

    public static void jackson_perf() throws Exception {
        DoubleArray20 benchmark = new DoubleArray20();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.jackson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("DoubleArray20-jackson : " + millis);
        // zulu8.62.0.19 : 3306
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

    public static void wastjson() throws Exception {
        DoubleArray20 benchmark = new DoubleArray20();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.wastjson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("DoubleArray20-wastjson : " + millis);
        // zulu8.62.0.19 : 3316
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

    public static void wastjson_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            jackson_perf();
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2_perf_test();
//        fastjson2_tree_perf_test();
        wastjson_test();
    }
}
