package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class DecimalSmallToStringTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final DecimalSmallToString benchmark = new DecimalSmallToString();
    static final int COUNT = 10_000_000;

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
            // zulu17.38.21 :
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
            // zulu17.38.21 :
        }
    }

    public static void layoutChars() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.layoutChars(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("DecimalToString-layoutChars millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // jdk22-ea : 109
            // jdk22-base : 162
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
            // zulu17.38.21 :
            // openjdk20 :
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
            // jdk21.0.0 :
            // openjdk22 :
        }
    }

    public static void main(String[] args) throws Throwable {
//        Random r = new Random();
//        int value = Math.abs(r.nextInt());
//        BigDecimal h = new BigDecimal("" + ((long) value + (long) Integer.MAX_VALUE)
//                + ((long) value + (long) Integer.MAX_VALUE) + ".55");
//        BigDecimal l = new BigDecimal("" + ((long) value + (long) Integer.MAX_VALUE) + ".55");
//        BigDecimal s = new BigDecimal("" + ((long) value / 1000) + ".55");

//        toPlainString();
//        toPlainStringDec();
        layoutChars();
//        toStringCharWithInt8();
//        toStringCharWithInt8UTF16();
    }
}
