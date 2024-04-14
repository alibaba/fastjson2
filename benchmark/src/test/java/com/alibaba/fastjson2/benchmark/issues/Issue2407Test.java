package com.alibaba.fastjson2.benchmark.issues;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class Issue2407Test {
    static final Issue2407 benchmark = new Issue2407();
    static final int COUNT = 10000;

    public static void parseArray() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.parseArray(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("Issue2407-parseArray millis : " + millis);
            // zulu17.40.19 : 2639
        }
    }

    public static void parseArray1() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.parseArray1(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("Issue2407-parseArray1 millis : " + millis);
            // zulu17.40.19 : 2461
        }
    }

    public static void main(String[] args) throws Exception {
        parseArray1();
    }
}
