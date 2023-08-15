package com.alibaba.fastjson2.benchmark.primitves;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class BigDecimalArrayWrite4Test {
    static final BigDecimalArrayWrite4 benchmark = new BigDecimalArrayWrite4();
    static final int COUNT = 10_000_000;

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("Date20-fastjson2 : " + millis);

            // zulu8.62.0.19 : 2531 2205
            // zulu11.64.19 : 2507 2332
            // zulu17.40.19 : 2500 2338
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

            // zulu8.70.0.23 :
            // zulu11.64.19 :
            // zulu17.40.19 :
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2();
//        fastjson2_str();
    }
}
