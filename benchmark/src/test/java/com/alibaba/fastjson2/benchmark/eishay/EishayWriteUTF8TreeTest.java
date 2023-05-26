package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayWriteUTF8TreeTest {
    static final EishayWriteUTF8BytesTree benchmark = new EishayWriteUTF8BytesTree();
    static final int LOOP = 1_000_000;

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu8.62.0.19 :
            // zulu11.52.13 :
            // zulu17.40.19 :
            // zulu17.40.19_vec :
        }
    }

    public static void fastjson2_ReferenceDetection() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2_ReferenceDetection(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2-fastjson2_ReferenceDetection millis : " + millis);
            // zulu8.62.0.19 : 624 596 591 575
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void fastjson2_jsonb() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2_jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2-jsonb millis : " + millis);
            // zulu8.62.0.19 : 443 397 391 372
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void jackson() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.jackson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("jackson millis : " + millis);
            // zulu8.62.0.19 : 604
            // zulu11.52.13 : 654
            // zulu17.32.13 : 706
        }
    }

    public static void wastjson() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.wastjson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("wastjson millis : " + millis);
            // zulu8.62.0.19 : 452
            // zulu11.52.13 : 475
            // zulu17.32.13 : 528
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2();
//        fastjson2_ReferenceDetection();
//        fastjson2_jsonb();
//        jackson();
//        wastjson();
//        gson();
    }
}
