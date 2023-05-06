package com.alibaba.fastjson2.benchmark.primitves;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class OffsetDateTime20Test {
    static final OffsetDateTime20 benchmark = new OffsetDateTime20();

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("OffsetDateTime20-fastjson2 : " + millis);

            // zulu8.62.0.19 : 2042
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void fastjson2_jsonb() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2_jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("OffsetDateTime20-fastjson2 : " + millis);

            // zulu8.62.0.19 :
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2();
//        fastjson2_jsonb();
//        fastjson1();
    }
}
