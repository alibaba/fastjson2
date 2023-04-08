package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class CSVCOVID19ListTest {
    static final CSVCOVID19List benchmark = new CSVCOVID19List();
    static final int COUNT = 5;

    public static void rowCount() throws Exception {
        for (int j = 0; j < COUNT; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10; ++i) {
                benchmark.rowCount(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("COVID19-rowCount millis : " + millis);
            // zulu8.62.0.19 :
        }
    }

    public static void readLines() throws Exception {
        for (int j = 0; j < COUNT; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10; ++i) {
                benchmark.readLines(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("COVID19-readLines millis : " + millis);
            // zulu8.62.0.19 :
        }
    }

    public static void readLineValues() throws Exception {
        for (int j = 0; j < COUNT; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10; ++i) {
                benchmark.readLineValues(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("COVID19-readLines millis : " + millis);
            // zulu8.68.0.21 : 1047
        }
    }

    public static void main(String[] args) throws Exception {
        rowCount();
    }
}
