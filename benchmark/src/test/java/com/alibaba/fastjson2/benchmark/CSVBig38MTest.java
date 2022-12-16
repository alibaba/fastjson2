package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class CSVBig38MTest {
    static final CSVBig38M benchmark = new CSVBig38M();
    static final int COUNT = 5;

    public static void rowCount() throws Exception {
        for (int j = 0; j < COUNT; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10; ++i) {
                benchmark.rowCount(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("CSVBig38M-rowCount millis : " + millis);
            // zulu8.62.0.19 : 336
        }
    }

    public static void readLines() throws Exception {
        for (int j = 0; j < COUNT; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10; ++i) {
                benchmark.readLines(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("CSVBig38M-readLines millis : " + millis);
            // zulu8.62.0.19 : 2218
        }
    }

    public static void readLineValues() throws Exception {
        for (int j = 0; j < COUNT; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10; ++i) {
                benchmark.readLineValues(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("CSVBig38M-readLines millis : " + millis);
            // zulu8.62.0.19 : 2267
        }
    }

    public static void main(String[] args) throws Exception {
        readLineValues();
    }
}
