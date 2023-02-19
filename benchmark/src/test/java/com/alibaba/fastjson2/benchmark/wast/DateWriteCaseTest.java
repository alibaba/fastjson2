package com.alibaba.fastjson2.benchmark.wast;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class DateWriteCaseTest {
    static final DateWriteCase benchmark = new DateWriteCase();
    static final int LOOP = 1_000_000;

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateWriteCase-fastjson2 : " + millis);

            // zulu8.68.0.21 : 1727 1642 679 570 363 252 250 148 143 121 119 117
            // zulu11.52.13 : 117
            // zulu17.40.19 : 118
        }
    }

    public static void wastjson() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.wastjson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DateWriteCase-wastjson : " + millis);

            // zulu8.68.0.21 : 288 156
            // zulu11.52.13 : 159
            // zulu17.49.19 : 134
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2();
        wastjson();
    }
}
