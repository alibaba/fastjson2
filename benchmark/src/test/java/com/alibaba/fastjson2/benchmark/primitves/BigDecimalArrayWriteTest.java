package com.alibaba.fastjson2.benchmark.primitves;

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
            System.out.println("Date20-fastjson2 : " + millis);

            // zulu8.62.0.19 : 2425 1917
            // zulu11.52.13 : 2422 2009
            // zulu17.40.19 : 2654 2072
        }
    }

    public static void fastjson2_str() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjson2_str(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("Date20-fastjson2 : " + millis);

            // zulu8.70.0.23 : 2350 1950
            // zulu11.64.19 : 2404 2085
            // zulu17.40.19 : 2424 2113
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2();
        fastjson2_str();
    }
}
