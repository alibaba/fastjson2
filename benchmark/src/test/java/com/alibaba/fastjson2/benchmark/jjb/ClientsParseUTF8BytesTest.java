package com.alibaba.fastjson2.benchmark.jjb;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class ClientsParseUTF8BytesTest {
    static final ClientsParseUTF8Bytes benchmark = new ClientsParseUTF8Bytes();

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu17.40.19 : 2850 2815 2777 2770 2730 2704 2717 2661 2638 2603 2587 2662
            // oracle-jdk-17.0.6 :
            // oracle-jdk-17.0.6_vec :
        }
    }

    public static void dsljson() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.dsljson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("dsljson millis : " + millis);
            // zulu17.40.19 : 3341 2505
        }
    }

    public static void jackson() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.jackson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("jackson millis : " + millis);
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2();
//        dsljson();
//        jackson();
    }
}
