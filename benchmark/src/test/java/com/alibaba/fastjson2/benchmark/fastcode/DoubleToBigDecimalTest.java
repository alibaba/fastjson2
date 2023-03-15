package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class DoubleToBigDecimalTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final DoubleToBigDecimal benchmark = new DoubleToBigDecimal();
    static final int COUNT = 10_000_000;

    public static void f0() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.f0(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DoubleToBigDecimal-f0 millis : " + millis);
            // zulu8.58.0.13 : 587
            // zulu11.52.13 :
            // zulu17.40.19 : // 528
        }
    }

    public static void f1() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.f1(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DoubleToBigDecimal-f1 millis : " + millis);
            // zulu8.58.0.13 : 635
            // zulu11.52.13 :
            // zulu17.40.19 : 644
        }
    }

    public static void main(String[] args) throws Throwable {
        f0();
//        f1();
    }
}
