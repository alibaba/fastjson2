package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayWriteBinaryTest {
    static final EishayWriteBinary benchmark = new EishayWriteBinary();

    public static void jsonb() throws Exception {
        System.out.println("jsonb size " + benchmark.jsonbSize()); // 362

        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("jsonb millis : " + millis);
            // zulu8.58.0.13 : 285
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
        jsonb();
//        protobuf();
    }
}
