package com.alibaba.fastjson2.benchmark.wast;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class SimpleBeanTest {
    public static void fastjson2_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2();
        }
    }

    public static void fastjson2() {
        SimpleBeanCase benchmark = new SimpleBeanCase();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; ++i) {
            benchmark.fastjson2(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("SimpleBean-fastjson2 : " + millis);

        // zulu8.62.0.19 : 837 465 439 447
        // zulu11.52.13 : 316
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

    public static void wastjson_test() {
        for (int i = 0; i < 10; i++) {
            wastjson();
        }
    }

    public static void wastjson() {
        SimpleBeanCase benchmark = new SimpleBeanCase();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; ++i) {
            benchmark.wastjson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("SimpleBean-wastjson : " + millis);

        // zulu8.62.0.19 : 483 290
        // zulu11.52.13 : 277
        // zulu17.32.13 :
        // zulu18.28.13 :
        // zulu19.0.47 :
        // corretto-8 :
        // corretto-11 :
        // corretto-17 :
        // corretto-18 : 365 291
        // oracle-jdk-17.0.4 :
        // oracle-jdk-18.0.2 :
    }

    public static void jackson_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            jackson();
        }
    }

    public static void jackson() throws Exception {
        SimpleBeanCase benchmark = new SimpleBeanCase();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; ++i) {
            benchmark.jackson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("SimpleBean-jackson : " + millis);

        // zulu8.62.0.19 : 1005
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu18.28.13 :
        // zulu19.0.47 :
        // corretto-8 :
        // corretto-11 :
        // corretto-17 :
        // corretto-18 : 1002
        // oracle-jdk-17.0.4 :
        // oracle-jdk-18.0.2 :
    }

    public static void main(String[] args) throws Exception {
//        fastjson2_test();
//        jackson_test();
        wastjson_test();
    }
}
