package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class HomepageTreeTest {
    public static void fastjson2_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_perf();
        }
    }

    public static void fastjson2_perf() {
        HomepageTree benchmark = new HomepageTree();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 10; ++i) {
            benchmark.fastjson2(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("HomepageTree-fastjson2 : " + millis);

        // zulu8.62.0.19 : 577
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

    public static void jackson_perf_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            jackson_perf();
        }
    }

    public static void jackson_perf() throws Exception {
        HomepageTree benchmark = new HomepageTree();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 10; ++i) {
            benchmark.jackson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("HomepageTree-jackson : " + millis);
        // zulu8.62.0.19 : 673
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
//        jackson_perf_test();
    }
}
