package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class CSVReaderCOVID19Test {
    static final CSVReaderCOVID19 benchmark = new CSVReaderCOVID19();
    static final int LOOP = 10;

    public static void fastjson2() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu8.68.0.21 : 561
            // zulu11.62.17 : 574
            // zulu17.40.19 : 521
        }
    }

    public static void csvReader() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.csvReader(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("csvReader millis : " + millis);
            // zulu8.68.0.21 : 769
            // zulu11.62.17 : 762
            // zulu17.40.19 : 713
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2();
//        csvReader();
    }
}
