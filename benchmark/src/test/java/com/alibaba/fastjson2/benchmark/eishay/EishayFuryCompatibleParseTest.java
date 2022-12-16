package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayFuryCompatibleParseTest {
    static final EishayFuryCompatibleParse benchmark = new EishayFuryCompatibleParse();
    static final int COUNT = 10_000_000;

    public static void fastjson2JSONB() throws Exception {
        System.out.println("EishayFuryParse-fastjson2_jsonb size " + benchmark.fastjson2JSONBBytes.length); // 409
        System.out.println();

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjson2JSONB(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryParse-fastjson2_jsonb millis : " + millis);
            // zulu8.58.0.13 : 3824
            // zulu11.52.13 : 3023
            // zulu17.38.21 : 3109
        }
    }

    public static void fury() throws Exception {
        System.out.println("EishayFuryParse-fury size " + benchmark.furyCompatibleBytes.length); // 670
        System.out.println();

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fury(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayFuryParse-fury millis : " + millis);
            // zulu8.58.0.13 : 3267
            // zulu11.52.13 : 3361
            // zulu17.38.21 : 3360
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2JSONB();
//        fury();
    }
}
