package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class CSVCOVID19Test {
    static final CSVCOVID19 benchmark = new CSVCOVID19();
    static final int LOOP = 10;

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu8.68.0.21 : 597
            // zulu11.62.17 : 514
            // zulu17.40.19 : 540
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
            // zulu8.68.0.21 : 619
            // zulu11.62.17 : 521
            // zulu17.40.19 : 541
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
//        fastjson2();
        univocity();
//        cainiao();
    }
}
