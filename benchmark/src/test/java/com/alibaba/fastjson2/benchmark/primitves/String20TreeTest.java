package com.alibaba.fastjson2.benchmark.primitves;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class String20TreeTest {
    public static void fastjson2_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_perf();
        }
    }

    public static void fastjson2_perf() {
        String20Tree benchmark = new String20Tree();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson2(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("String20Tree-fastjson2 : " + millis);

        // zulu8.62.0.19 : 693
        // zulu11.52.13 : 626
        // zulu17.32.13 : 615
        // zulu18.28.13 : 574
        // zulu19.0.47 : 577
        // corretto-8 : 707
        // corretto-11 : 621
        // corretto-17 : 617
        // corretto-17 : 546
        // oracle-jdk-17.0.4 : 610
        // oracle-jdk-18.0.2 : 575
    }

    public static void jackson_perf_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            jackson_perf();
        }
    }

    public static void jackson_perf() throws Exception {
        String20Tree benchmark = new String20Tree();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.jackson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("String20Tree-jackson : " + millis);
        // zulu8.62.0.19 : 894
        // zulu11.52.13 : 1088
        // zulu17.32.13 : 982
        // zulu18.28.13 : 1017
        // zulu19.0.47 : 1006
        // corretto-8 : 889
        // corretto-11 : 1121
        // corretto-17 : 1013
        // corretto-18 : 1045
        // oracle-jdk-17.0.4 : 994
        // oracle-jdk-18.0.2 : 998
    }

    public static void main(String[] args) throws Exception {
        fastjson2_perf_test();
//        jackson_perf_test();
    }
}
