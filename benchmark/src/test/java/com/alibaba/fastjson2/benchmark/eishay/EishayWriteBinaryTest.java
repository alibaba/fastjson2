package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayWriteBinaryTest {
    static final EishayWriteBinary benchmark = new EishayWriteBinary();

    public static void fastjson2JSONB_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            fastjson2JSONB();
        }
    }

    public static void fastjson2JSONB() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson2JSONB(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("fastjson2_jsonb millis : " + millis);
        // zulu8.58.0.13 : 285
        // zulu11.52.13 :
        // zulu17.32.13 :
    }

    public static void main(String[] args) throws Exception {
        fastjson2JSONB_test();
//        ffastjson2JSONBArrayMapping_test();
//        kryo_test();
    }
}
