package com.alibaba.fastjson2.benchmark.eishay;

import org.openjdk.jmh.infra.Blackhole;

public class EishayWriteUTF8BytesTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final EishayWriteUTF8Bytes benchmark = new EishayWriteUTF8Bytes();

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
        // zulu8.58.0.13 : 336
        // zulu11.52.13 : 337
        // zulu17.32.13 :
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
        // zulu8.58.0.13 : 641
        // zulu11.52.13 : 721
        // zulu17.32.13 :
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
        System.out.println("gson millis : " + millis);
        // zulu8.58.0.13 : 1569
        // zulu11.52.13 :
        // zulu17.32.13 :
    }

    public static void main(String[] args) throws Exception {
//        fastjson2_test();
        gson_test();
//        jackson_perf_test();
    }
}
