package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class HomepageTreeTest {
    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            HomepageTree benchmark = new HomepageTree();

            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 10; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("HomepageTree-fastjson2 : " + millis);

            // zulu8.62.0.19 : 577 550
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
    }

    public static void fastjson2_jsonb() throws Exception {
        for (int j = 0; j < 10; j++) {
            HomepageTree benchmark = new HomepageTree();

            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 10; ++i) {
                benchmark.fastjson2_jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("HomepageTree-fastjson2_jsonb : " + millis);
            // zulu8.62.0.19 : 673 326
            // zulu11.52.13 : 400
            // zulu17.32.13 : 306
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

    public static void jackson() throws Exception {
        for (int j = 0; j < 10; j++) {
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
    }

    public static void wastjson() throws Exception {
        for (int j = 0; j < 10; j++) {
            HomepageTree benchmark = new HomepageTree();

            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 10; ++i) {
                benchmark.wastjson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("HomepageTree-wastjson : " + millis);
            // zulu8.62.0.19 : 361
            // zulu11.52.13 : 498
            // zulu17.32.13 : 468
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

    public static void main(String[] args) throws Exception {
//        fastjson2();
        fastjson2_jsonb();
//        jackson();
//        wastjson();
    }
}
