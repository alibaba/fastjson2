package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayParseUTF8BytesTest {
    static final EishayParseUTF8Bytes benchmark = new EishayParseUTF8Bytes();

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu8.62.0.19 : 703 746 710 706 700 682
            // zulu11.52.13 : 579 565 552 541 554
            // zulu17.32.13 : 563 545 531 551
        }
    }

    public static void dsljson() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.dsljson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("dsljson millis : " + millis);
            // zulu8.62.0.19 : 658
            // zulu11.52.13 : 818
            // zulu17.32.13 : 854
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
            // zulu8.62.0.19 : 963
            // zulu11.52.13 : 1058
            // zulu17.32.13 : 1064
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2();
        dsljson();
//        jackson();
    }
}
