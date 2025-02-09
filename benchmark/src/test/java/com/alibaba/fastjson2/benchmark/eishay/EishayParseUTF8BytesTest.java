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
            // zulu8.62.0.19 : 703 746 710 706 700 682 717 698 526 500 474 445 425
            // zulu11.52.13 : 579 565 552 541 554 553 554 538 420 424 434 370
            // zulu17.40.19 : 600 604 597 593 578 567 447 420 380
        }
    }

    public static void fastjson2_features() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2_features(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2_features millis : " + millis);
            // zulu8.62.0.19 :
            // zulu11.52.13 :
            // zulu17.40.19 : 376
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
            // zulu17.32.13 : 854 1050
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
            // zulu17.32.13 : 1064 1175
            // graalvm_17.0.7 600
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2();
//        fastjson2_features();
//        dsljson();
//        jackson();
    }
}
