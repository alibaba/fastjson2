package com.alibaba.fastjson2.benchmark.eishay;

import org.openjdk.jmh.infra.Blackhole;

public class EishayParseTreeStringTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");

    public static void fastjson2_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_perf();
        }
    }

    public static void fastjson2_perf() {
        EishayParseTreeString perf = new EishayParseTreeString();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            perf.fastjson2(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastjson2 millis : " + millis);
        // zulu8.62.0.19 : 666
        // zulu11.52.13 : 821
        // zulu17.32.13 : 601
        // zulu18.28.13 : 598
        // zulu19.0.47 :
        // corretto-8 :
        // corretto-11 :
        // corretto-17 :
        // corretto-18 :
        // oracle-jdk-17.0.4 :
        // oracle-jdk-18.0.2 :
        // ibm-aarch64_mac_11.0.15_10 : 1240
        // ibm-aarch64_mac_17.0.3_7 : 1311
    }

    public static void jackson_perf_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            jackson_perf();
        }
    }

    public static void jackson_perf() throws Exception {
        EishayParseTreeString perf = new EishayParseTreeString();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            perf.jackson(BH);
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

    public static void wastjson_perf_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            wastjson_perf();
        }
    }

    public static void wastjson_perf() throws Exception {
        EishayParseTreeString perf = new EishayParseTreeString();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            perf.wastjson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("jackson millis : " + millis);
        // zulu8.62.0.19 : 1082
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
//        wastjson_perf_test();
    }
}
