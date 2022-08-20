package com.alibaba.fastjson2.benchmark.eishay;

import org.openjdk.jmh.infra.Blackhole;

public class EishayParseStringTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final EishayParseString benchmark = new EishayParseString();

    public static void fastjson2_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2();
        }
    }

    public static void fastjson2() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson2(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastjson2 millis : " + millis);
        // zulu8.62.0.19 : 666 578 554
        // zulu11.52.13 : 821 546 521 494 492 502
        // zulu17.32.13 : 601 552 532 516 511
        // zulu18.28.13 : 598 555 534 527 512
        // zulu19.0.75 : 515
        // corretto-8 : 558
        // corretto-11 : 516
        // corretto-17 : 512
        // corretto-18 : 510
        // oracle-jdk-17.0.4 : 562 536
        // oracle-jdk-18.0.2 : 552 530
        // ibm-aarch64_mac_11.0.15_10 : 1240
        // ibm-aarch64_mac_17.0.3_7 : 1311
        // graalvm-ce-java17-22.2.0 : 711
        // graalvm-ee-java17-22.2.0 : 589
    }

    public static void fastjson1_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            fastjson1();
        }
    }

    public static void fastjson1() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson1(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastjson1 millis : " + millis);
        // zulu8.62.0.19 : 664
        // zulu11.52.13 : 677
        // zulu17.32.13 : 522
        // zulu18.28.13 :
        // zulu19.0.47 :
        // corretto-8 :
        // corretto-11 :
        // corretto-17 :
        // corretto-18 :
        // oracle-jdk-17.0.4 :
        // oracle-jdk-18.0.2 :
    }

    public static void jackson_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            jackson();
        }
    }

    public static void jackson() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.jackson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("jackson millis : " + millis);
        // zulu8.62.0.19 : 1266
        // zulu11.52.13 : 1314
        // zulu17.32.13 : 1246
        // zulu18.28.13 : 1279
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
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.wastjson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("EishayParseString-wastjson millis : " + millis);
        // zulu8.62.0.19 : 466
        // zulu11.52.13 : 466
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

    public static void gson_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            gson();
        }
    }

    public static void gson() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.gson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("EishayParseString-wastjson millis : " + millis);
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

    public static void main(String[] args) throws Exception {
        fastjson2_test();
//        jackson_test();
//        fastjson1_test();
//        gson_test();
//        wastjson_test();
    }
}
