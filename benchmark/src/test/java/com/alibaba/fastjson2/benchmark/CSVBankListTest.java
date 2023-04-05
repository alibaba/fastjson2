package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class CSVBankListTest {
    static final CSVBankList benchmark = new CSVBankList();
    static final int COUNT = 5;

    public static void rowCount() throws Exception {
        for (int j = 0; j < COUNT; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10; ++i) {
                benchmark.rowCount(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BankList-rowCount millis : " + millis);
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
            System.out.println("BankList-readLines millis : " + millis);
            // zulu8.62.0.19 :
        }
    }

    public static void readLineValues() throws Exception {
        for (int j = 0; j < COUNT; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000; ++i) {
                benchmark.readLineValues(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BankList-readLines millis : " + millis);
            // zulu8.68.0.21 237
        }
    }

    public static void main(String[] args) throws Exception {
        readLineValues();
    }
}
