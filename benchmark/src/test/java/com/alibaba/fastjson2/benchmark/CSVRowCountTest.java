package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class CSVRowCountTest {
    static final CSVRowCount benchmark = new CSVRowCount();
    static final int COUNT = 5;

    public static void big() throws Exception {
        for (int j = 0; j < COUNT; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10; ++i) {
                benchmark.big(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("CSVRowCount-big millis : " + millis);
            // zulu8.62.0.19 : 419 393
        }
    }

    public static void main(String[] args) throws Exception {
        big();
    }
}
