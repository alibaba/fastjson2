package com.alibaba.fastjson2.benchmark.eishay;

import org.openjdk.jmh.infra.Blackhole;

public class EishayParseTreeUTF8BytesTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");

    public static void fastjson2_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_perf();
        }
    }

    public static void fastjson2_perf() {
        EishayParseTreeUTF8Bytes perf = new EishayParseTreeUTF8Bytes();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            perf.fastjson2(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastjson2 millis : " + millis);
        // zulu17.32.13 :
        // zulu11.52.13 :
        // zulu8.58.0.13 : 995
    }

    public static void jackson_perf_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            jackson_perf();
        }
    }

    public static void jackson_perf() throws Exception {
        EishayParseTreeUTF8Bytes perf = new EishayParseTreeUTF8Bytes();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            perf.jackson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("jackson millis : " + millis);
        // zulu17.32.13 :
        // zulu11.52.13 :
        // zulu8.58.0.13 :
    }

    public static void main(String[] args) throws Exception {
        fastjson2_perf_test();
        jackson_perf_test();
    }
}
