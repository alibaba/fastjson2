package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayFuryWriteNoneCacheTest {
    static final EishayFuryWriteNoneCache benchmark = new EishayFuryWriteNoneCache();

    static final int COUNT = 1000;
    public static void fastjson2JSONB() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjson2JSONB(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryWriteArray-fastjson2_jsonb millis : " + millis);
            // zulu8.62.0.19 : 684
            // zulu11.52.13 : 507
            // zulu17.38.21 : 435
        }
    }

    public static void fury() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fury(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryWriteArray-fury millis : " + millis);
            // zulu8.62.0.19 : 15662
            // zulu11.52.13 : 11248
            // zulu17.38.21 : 9946
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2JSONB();
        fury();
    }
}
