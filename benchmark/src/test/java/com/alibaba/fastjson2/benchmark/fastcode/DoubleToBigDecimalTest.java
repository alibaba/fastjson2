package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class DoubleToBigDecimalTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final DoubleToBigDecimal benchmark = new DoubleToBigDecimal();
    static final int COUNT = 10_000_000;

    public static void fastjson2() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DoubleToBigDecimal-fastjson2 millis : " + millis);
            // zulu8.58.0.13 : 587
            // zulu11.52.13 :
            // zulu17.40.19 : 528
            // grallvm-ce-17-22.3.1 : 531
            // grallvm-e-17-22.3.1 : 417
        }
    }

    public static void jdk() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.jdk(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DoubleToBigDecimal-jdk millis : " + millis);
            // zulu8.58.0.13 : 635
            // zulu11.52.13 :
            // zulu17.40.19 : 644
        }
    }

    public static void main(String[] args) throws Throwable {
        fastjson2();
//        jdk();
    }
}
