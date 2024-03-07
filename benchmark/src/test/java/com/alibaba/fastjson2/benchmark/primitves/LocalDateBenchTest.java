package com.alibaba.fastjson2.benchmark.primitves;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class LocalDateBenchTest {
    static final LocalDateBench benchmark = new LocalDateBench();
    static final int LOOP = 100_000;

    public static void utf8() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.utf8(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("LocalDateBench-utf8 : " + millis);

            // zulu8.62.0.19 :
            // zulu11.52.13 :
            // zulu17.32.13 : 592
        }
    }

    public static void utf16() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.utf16(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("LocalDateBench-utf16 : " + millis);

            // zulu8.62.0.19 :
            // zulu11.52.13 :
            // zulu17.32.13 : 648
        }
    }

    public static void main(String[] args) throws Exception {
//        utf8();
        utf16();
    }
}
