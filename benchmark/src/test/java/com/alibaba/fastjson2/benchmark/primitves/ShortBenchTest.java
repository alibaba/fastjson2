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

    public static void bean_jsonBytes() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000; ++i) {
                benchmark.bean_jsonBytes(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ShortBench-bean_jsonBytes : " + millis);

            // zulu8.62.0.19 :
            // zulu11.52.13 :
            // zulu17.32.13 : 370
        }
    }

    public static void bean_jsonStr() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000; ++i) {
                benchmark.bean_jsonStr(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ShortBench-bean_jsonStr : " + millis);

            // zulu8.62.0.19 :
            // zulu11.52.13 :
            // zulu17.32.13 : 560
        }
    }

    public static void bean_jsonb() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000; ++i) {
                benchmark.bean_jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ShortBench-bean_jsonb : " + millis);

            // zulu8.62.0.19 :
            // zulu11.52.13 :
            // zulu17.32.13 : 361
        }
    }

    public static void main(String[] args) throws Exception {
//        utf8();
//        utf16();
//        bean_jsonBytes();
//        bean_jsonStr();
        bean_jsonb();
    }
}
