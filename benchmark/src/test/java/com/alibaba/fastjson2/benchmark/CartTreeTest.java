package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class CartTreeTest {
    static final CartTree benchmark = new CartTree();

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 10; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("CartTree-fastjson2 : " + millis);

            // zulu8.62.0.19 : 1332 1212
            // zulu11.52.13 : 1515
            // zulu17.32.13 : 1266 1165
            // zulu18.28.13 : 1245
            // zulu19.0.47 :
            // corretto-8 :
            // corretto-11 :
            // corretto-17 :
            // corretto-18 :
            // oracle-jdk-17.0.4 :
            // oracle-jdk-18.0.2 :
        }
    }

    public static void fastjson2_jsonb() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 10; ++i) {
                benchmark.fastjson2_jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("CartTree-fastjson2 : " + millis);

            // zulu8.62.0.19 : 940 833 775 737
            // zulu11.52.13 : 1112 1008 1003 979
            // zulu17.32.13 : 1060 731 687
            // zulu18.28.13 :
            // zulu19.0.47 :
            // corretto-8 :
            // corretto-11 :
            // corretto-17 :
            // corretto-18 :
            // oracle-jdk-17.0.4 :
            // oracle-jdk-18.0.2 :
        }
    }

    public static void jackson_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            jackson();
        }
    }

    public static void jackson() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 10; ++i) {
            benchmark.jackson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("CartTree-jackson : " + millis);
        // zulu8.62.0.19 : 1596
        // zulu11.52.13 : 1910
        // zulu17.32.13 : 1674
        // zulu18.28.13 : 1733
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
            wastjson();
        }
    }

    public static void wastjson() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 10; ++i) {
            benchmark.wastjson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("CartTree-wastjson : " + millis);
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
//        fastjson2();
        fastjson2_jsonb();
//        jackson_perf_test();
//        wastjson_test();
    }
}
