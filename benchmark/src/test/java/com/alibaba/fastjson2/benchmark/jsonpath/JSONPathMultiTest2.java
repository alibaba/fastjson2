package com.alibaba.fastjson2.benchmark.jsonpath;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class JSONPathMultiTest2 {
    static final JSONPathMultiBenchmark2 benchmark = new JSONPathMultiBenchmark2();
    static final int COUNT = 1_000_000;

    public static void evalMulti() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.evalMulti(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("JSONPathMultiBenchmark2-evalMulti millis : " + millis);
        }
        // zulu8.62.0.19 : 1099
    }

    public static void eval() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.eval(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("JSONPathMultiBenchmark2-eval millis : " + millis);
        }
        // zulu8.62.0.19 : 1113
    }

    public static void extract() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.extract(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("JSONPathMultiBenchmark2-extract millis : " + millis);
        }
        /// zulu8.62.0.19 : 792
    }

    public static void main(String[] args) throws Exception {
        eval();
    }
}
