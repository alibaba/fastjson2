package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayFuryParseTest {
    static final EishayFuryParse benchmark = new EishayFuryParse();

    public static void fury() throws Exception {
        System.out.println("EishayFuryParse-fury size " + benchmark.furyCompatibleBytes.length); // 670
        System.out.println();

        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.furyCompatible(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryParse-fury millis : " + millis);
            // zulu8.58.0.13 : 330
            // zulu11.52.13 : 490
            // zulu17.38.21 : 335
        }
    }

    public static void fastjson2JSONB() throws Exception {
        System.out.println("EishayFuryParse-fastjson2_jsonb size " + benchmark.fastjson2JSONBBytes.length); // 409
        System.out.println();

        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2JSONB(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryParse-fastjson2_jsonb millis : " + millis);
            // zulu8.58.0.13 : 369
            // zulu11.52.13 : 310
            // zulu17.38.21 : 316 314 313
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2JSONB();
//        fury();
    }
}
