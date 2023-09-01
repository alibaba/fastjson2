package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class DecimalToStringTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final DecimalToString benchmark = new DecimalToString();
    static final int COUNT = 100_000_000;

    public static void toPlainString() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.toPlainString(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DecimalToString-toPlainStringDec millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.38.21 : 832 785
        }
    }

    public static void toPlainStringDec() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.toPlainStringDec(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DecimalToString-toPlainStringDec millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.38.21 : 1765
        }
    }

    public static void toStringCharWithInt8() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.toStringCharWithInt8(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DecimalToString-toStringCharWithInt8 millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.38.21 : 8718
            // openjdk20 : 5514
        }
    }

    public static void toStringCharWithInt8UTF16() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.toStringCharWithInt8UTF16(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DecimalToString-toStringCharWithInt8UTF16 millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // jdk21.0.0 : 5722
            // openjdk22 : 4684
        }
    }

    public static void main(String[] args) throws Throwable {
//        toPlainString();
//        toPlainStringDec();
        toStringCharWithInt8();
//        toStringCharWithInt8UTF16();
    }
}
