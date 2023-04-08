package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class CSVBankTest {
    static final CSVBank benchmark = new CSVBank();
    static final int LOOP = 1000;

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu8.68.0.21 : 235
            // zulu11.62.17 : 158
            // zulu17.40.19 : 161
        }
    }

    public static void univocity() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.univocity(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("univocity millis : " + millis);
            // zulu8.68.0.21 : 378
            // zulu11.62.17 : 320
            // zulu17.40.19 : 280
        }
    }

    public static void cainiao() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.cainiao(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("cainiao millis : " + millis);
            // zulu8.68.0.21 :
            // zulu11.62.17 :
            // zulu17.40.19 :
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2();
//        univocity();
//        cainiao();
    }
}
