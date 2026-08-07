package com.alibaba.fastjson2.benchmark;

import org.openjdk.jmh.infra.Blackhole;

public class ASCIICheckTest {
    static final Blackhole BH = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");

    public void f0_perf() {
        ASCIICheck benchmark = new ASCIICheck();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000 * 100; ++i) {
            benchmark.f0_vec(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("f0 millis : " + millis);
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu8.58.0.13 :
    }

    public void f0_perf_test() {
        for (int i = 0; i < 10; i++) {
            f0_perf(); //
        }
    }

    public void f1_perf() {
        ASCIICheck benchmark = new ASCIICheck();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000 * 100; ++i) {
            benchmark.f1(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("f1 millis : " + millis);
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu8.58.0.13 :
    }

    public void f1_perf_test() {
        for (int i = 0; i < 10; i++) {
            f1_perf(); //
        }
    }

    public static void main(String[] args) throws Exception {
        ASCIICheckTest benchmark = new ASCIICheckTest();
        benchmark.f0_perf_test();
        benchmark.f1_perf_test();
    }
}
