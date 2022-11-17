package com.alibaba.fastjson2.benchmark.jsonpath;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class JSONPathMultiTest {
    static final JSONPathMultiBenchmark benchmark = new JSONPathMultiBenchmark();
    static final int COUNT = 1_000_000;

    public static void evalMulti() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.evalMulti(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("JSONPathMultiBenchmark-multiEval millis : " + millis);
        }
        // zulu8.62.0.19 : 1034
    }

    public static void eval() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.eval(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("JSONPathMultiBenchmark-multiEval millis : " + millis);
        }
        // zulu8.62.0.19 : 1056
    }

    public static void extract() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.extract(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("JSONPathMultiBenchmark-multiEval millis : " + millis);
        }
        /// zulu8.62.0.19 : 656
    }

    public static void main(String[] args) throws Exception {
        extract();
    }
}
