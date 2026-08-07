package com.alibaba.fastjson2.benchmark.wast;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayWriterCaseTest {
    static final EishayWriterCase benchmark = new EishayWriterCase();
    static final int LOOP = 1_000_000;

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayWriterCase-fastjson2 : " + millis);

            // zulu8.68.0.21 : 630 566 537 523 511 525
            // zulu11.52.13 : 506
            // zulu17.40.19 : 516
        }
    }

    public static void wastjson() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.wastjson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayWriterCase-wastjson : " + millis);

            // zulu8.68.0.21 : 492 464
            // zulu11.52.13 : 479
            // zulu17.40.19 : 525
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2();
        wastjson();
    }
}
