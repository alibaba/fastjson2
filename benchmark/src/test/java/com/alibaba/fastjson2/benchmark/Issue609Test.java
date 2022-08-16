package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class Issue609Test {
    public static void fastJSON1ArrayDeTime_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            fastJSON1ArrayDeTime();
        }
    }

    public static void fastJSON1ArrayDeTime() throws Exception {
        Issue609 benchmark = new Issue609();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            benchmark.fastJSON1ArrayDeTime(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastJSON1ArrayDeTime : " + millis);
        // zulu8.62.0.19 : 2365
        // zulu11.52.13 : 2155
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

    public static void fastJSON2ArrayDeTime_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            fastJSON2ArrayDeTime();
        }
    }

    public static void fastJSON2ArrayDeTime() throws Exception {
        Issue609 benchmark = new Issue609();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            benchmark.fastJSON2ArrayDeTime(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastJSON2ArrayDeTime : " + millis);
        // zulu8.62.0.19 : 1473
        // zulu11.52.13 : 1227
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

    public static void fastJSON2ObjSeTime_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            fastJSON2ObjSeTime();
        }
    }

    public static void fastJSON2ObjSeTime() throws Exception {
        Issue609 benchmark = new Issue609();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            benchmark.fastJSON2ObjSeTime(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastJSON2ArrayDeTime : " + millis);
        // zulu8.62.0.19 : 1055
        // zulu11.52.13 : 3012 2222 2220 1153
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

    public static void fastJSON1ObjSeTime_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            fastJSON1ObjSeTime();
        }
    }

    public static void fastJSON1ObjSeTime() throws Exception {
        Issue609 benchmark = new Issue609();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            benchmark.fastJSON1ObjSeTime(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastJSON1ArrayDeTime : " + millis);
        // zulu8.62.0.19 : 1737
        // zulu11.52.13 : 1820
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
//        fastJSON2ObjSeTime_test();
        fastJSON1ObjSeTime_test();
    }
}
