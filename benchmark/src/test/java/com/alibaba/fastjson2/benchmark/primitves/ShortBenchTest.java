package com.alibaba.fastjson2.benchmark.primitves;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class ShortBenchTest {
    static final ShortBench benchmark = new ShortBench();

    public static void utf8() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000; ++i) {
                benchmark.utf8(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ShortBench-utf8 : " + millis);

            // zulu8.62.0.19 : 266
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void utf16() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000; ++i) {
                benchmark.utf16(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ShortBench-utf16 : " + millis);

            // zulu8.62.0.19 : 399
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
//        utf8();
        utf16();
    }
}
