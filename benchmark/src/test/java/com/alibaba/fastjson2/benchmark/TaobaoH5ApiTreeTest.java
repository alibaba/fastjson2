package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class TaobaoH5ApiTreeTest {
    public static void fastjson2_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_perf();
        }
    }

    public static void fastjson2_perf() {
        TaobaoH5ApiTree benchmark = new TaobaoH5ApiTree();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 10; ++i) {
            benchmark.fastjson2(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("TaobaoH5ApiTree-fastjson2 : " + millis);

        // zulu8.62.0.19 : 1940 1592
        // zulu11.52.13 : 2836 1701
        // zulu17.32.13 : 1516
        // zulu18.28.13 : 1527
        // zulu19.0.47 :
        // corretto-8 :
        // corretto-11 :
        // corretto-17 :
        // corretto-18 :
        // oracle-jdk-17.0.4 :
        // oracle-jdk-18.0.2 :
    }

    public static void jackson_perf_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            jackson_perf();
        }
    }

    public static void jackson_perf() throws Exception {
        TaobaoH5ApiTree benchmark = new TaobaoH5ApiTree();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 10; ++i) {
            benchmark.jackson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("TaobaoH5ApiTree-jackson : " + millis);
        // zulu8.62.0.19 : 2623
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu18.28.13 : 2631
        // zulu19.0.47 :
        // corretto-8 :
        // corretto-11 :
        // corretto-17 :
        // corretto-18 :
        // oracle-jdk-17.0.4 :
        // oracle-jdk-18.0.2 :
    }

    public static void wastjson_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            wastjson();
        }
    }

    public static void wastjson() throws Exception {
        TaobaoH5ApiTree benchmark = new TaobaoH5ApiTree();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 10; ++i) {
            benchmark.wastjson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("TaobaoH5ApiTree-wastjson : " + millis);
        // zulu8.62.0.19 : 1121
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu18.28.13 : 1268
        // zulu19.0.47 :
        // corretto-8 :
        // corretto-11 :
        // corretto-17 :
        // corretto-18 :
        // oracle-jdk-17.0.4 :
        // oracle-jdk-18.0.2 :
    }

    public static void main(String[] args) throws Exception {
        fastjson2_perf_test();
//        jackson_perf_test();
//        wastjson_test();
    }
}
