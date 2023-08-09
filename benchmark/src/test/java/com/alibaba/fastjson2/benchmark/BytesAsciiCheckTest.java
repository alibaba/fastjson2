package com.alibaba.fastjson2.benchmark;

import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class BytesAsciiCheckTest {
    static final BytesAsciiCheck benchmark = new BytesAsciiCheck();
    static final int LOOP_COUNT = 10_000_000;

    public static void handler() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; ++i) {
                benchmark.handler(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BytesAsciiCheck-handler : " + millis);

            // zulu17.40.19 : 150
        }
    }

    public static void lambda() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; ++i) {
                benchmark.lambda(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BytesAsciiCheck-lambda : " + millis);

            // zulu17.40.19 : 118
        }
    }

    public static void direct() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; ++i) {
                benchmark.direct(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BytesAsciiCheck-direct : " + millis);

            // zulu17.40.19 : 1156
        }
    }

    public static void direct8() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; ++i) {
                benchmark.direct8(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BytesAsciiCheck-direct8 : " + millis);

            // zulu17.40.19 : 478
        }
    }

    @Test
    public void test() {
//        System.out.println(Integer.toHexString(-1) + "\t" + Integer.toBinaryString(-1));
//        System.out.println(Integer.toHexString(-2) + "\t" + Integer.toBinaryString(-2));
//        System.out.println(Integer.toHexString(Integer.MIN_VALUE) + "\t" + Integer.toBinaryString(Integer.MIN_VALUE));
//        System.out.println(Integer.toHexString(Integer.MIN_VALUE + 1) + "\t" + Integer.toBinaryString(Integer.MIN_VALUE + 1));
        long x = 0x8080808080808080L;
        System.out.println(Long.toBinaryString(x));
        System.out.println(x);
    }

    public static void main(String[] args) throws Throwable {
//        handler();
//        lambda();
//        direct();
//        direct8();
    }
}
