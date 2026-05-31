package com.alibaba.fastjson2.benchmark.primitives;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class BigDecimalArrayWriteTest {
    static final BigDecimalArrayWrite benchmark = new BigDecimalArrayWrite();
    static final int COUNT = 10_000_000;

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimalArrayWrite-fastjson2 : " + millis);

            // zulu8.62.0.19 : 2425 1917 1941
            // zulu11.52.13 : 2422 2009 2077
            // zulu17.40.19 : 2654 2072 2148
        }
    }

    public static void fastjson2_str() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjson2_str(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimalArrayWrite-fastjson2_str : " + millis);

            // zulu8.70.0.23 : 2350 1950 1930
            // zulu11.64.19 : 2404 2085 2124
            // zulu17.40.19 : 2424 2113 2175
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2();
//        fastjson2_str();
    }
}
