package com.alibaba.fastjson2.benchmark.wast;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class Int32ValueBeanCaseTest {
    static final Int32ValueBeanCase benchmark = new Int32ValueBeanCase();
    static final int LOOP = 1_000_000;

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("Int32ValueBeanCase-fastjson2 : " + millis);

            // zulu8.68.0.21 : 191
            // zulu11.52.13 : 185
            // zulu17.32.13 : 186
        }
    }

    public static void wastjson() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.wastjson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("Int32ValueBeanCase-wastjson : " + millis);

            // zulu8.68.0.21 : 186 136
            // zulu11.52.13 : 155
            // zulu17.32.13 : 156
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2();
        wastjson();
    }
}
