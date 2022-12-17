package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

public class InvokeFirstTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    static final InvokeFirst benchmark = new InvokeFirst();
    static final int COUNT = 10_000;

    public static void genLambda() throws Throwable {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.genLambda(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("InvokeFirst-genLambda millis : " + millis);
            // zulu8.58.0.13 : 3824
            // zulu11.52.13 : 3023
            // zulu17.38.21 : 3109
        }
    }

    public static void main(String[] args) throws Throwable {
        genLambda();
    }
}
