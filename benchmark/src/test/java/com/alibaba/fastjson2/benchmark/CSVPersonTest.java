package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class CSVPersonTest {
    static final CSVPerson benchmark = new CSVPerson();
    static final int LOOP = 100;

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu8.68.0.21 : 3621
            // zulu11.62.17 : 2576
            // zulu17.40.19 : 3501
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
            // zulu8.68.0.21 : 4426
            // zulu11.62.17 : 4645
            // zulu17.40.19 : 3622
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
            // zulu8.68.0.21 : 2650
            // zulu11.62.17 : 2881
            // zulu17.40.19 : 2861
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2();
        cainiao();
//        univocity();
    }
}
