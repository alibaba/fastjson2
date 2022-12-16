package com.alibaba.fastjson2.benchmark.eishay;

import org.openjdk.jmh.infra.Blackhole;

public class EishayParseStringNoneCacheTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final EishayParseStringNoneCache benchmark = new EishayParseStringNoneCache();
    static final int LOOP = 1000;

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu8.62.0.19 : 965 922 918 865 846
            // zulu11.52.13 : 753
            // zulu17.32.13 : 580
            // zulu18.28.13 :
            // zulu19.28.81 : 595
            // corretto-8 :
            // corretto-11 :
            // corretto-17 :
            // corretto-18 :
            // oracle-jdk-17.0.4 : 594
            // oracle-jdk-18.0.2 :
            //

            // reflect-zulu8.62.0.19
            // reflect-zulu11.52.13 :
            // reflect-zulu17.32.13 :
        }
    }

    public static void fastjson1() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson1(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson1 millis : " + millis);
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
    }

    public static void jackson() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.jackson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("jackson millis : " + millis);
            // zulu8.62.0.19 :
            // zulu11.52.13 :
            // zulu17.32.13 :
            // zulu18.28.13 :
            // zulu19.28.81 :
            // corretto-8 :
            // corretto-11 :
            // corretto-17 :
            // corretto-18 :
            // oracle-jdk-17.0.4 :
            // oracle-jdk-18.0.2 :
        }
    }

    public static void gson() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.gson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseString-gson millis : " + millis);
            // zulu8.62.0.19 : 1449
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

    public static void main(String[] args) throws Exception {
        fastjson2();
//        jackson_test();
//        fastjson1_test();
//        gson_test();
//        wastjson();
    }
}
