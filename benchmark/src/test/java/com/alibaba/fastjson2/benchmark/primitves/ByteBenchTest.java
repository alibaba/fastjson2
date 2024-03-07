package com.alibaba.fastjson2.benchmark.primitves;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class ByteBenchTest {
    static final ByteBench benchmark = new ByteBench();

    public static void utf8() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.utf8(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ByteBench-utf8 : " + millis);

            // zulu8.62.0.19 : 994 508 439
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void utf16() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.utf16(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ByteBench-utf16 : " + millis);

            // zulu8.62.0.19 : 459
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void jsonb() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ByteBench-jsonb : " + millis);

            // zulu8.62.0.19 : 37
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
//        utf8();
        utf16();
//        jsonb();
    }
}
