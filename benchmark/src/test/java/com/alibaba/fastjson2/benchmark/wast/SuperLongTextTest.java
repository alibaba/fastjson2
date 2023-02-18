package com.alibaba.fastjson2.benchmark.wast;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class SuperLongTextTest {
    static final SuperLongText benchmark = new SuperLongText();
    static final int LOOP = 1;

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayWriterCase-fastjson2 : " + millis);

            // zulu8.62.0.19 : 1819 1695 1622 1604
            // zulu11.52.13 :
            // oracle-jdk-17.0.6 1755 1640
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

            // zulu8.62.0.19 : 394
            // zulu11.52.13 :
            // oracle-jdk-17.0.6 331
        }
    }

    public static void jackson() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.jackson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayWriterCase-jackson : " + millis);

            // zulu8.62.0.19 :
            // zulu11.52.13 :
            // oracle-jdk-17.0.6 1831
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2();
//        wastjson();
//        jackson();
    }
}
