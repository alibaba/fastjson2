package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayWriteBinaryAutoTypeTest {
    static final EishayWriteBinaryAutoType benchmark = new EishayWriteBinaryAutoType();

    public static void hessian() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.hessian(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayWriteBinaryAutoType-hessian millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.32.13 : 1418
        }
    }

    public static void fastjson2UTF8Bytes() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2UTF8Bytes(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayWriteBinaryAutoType-fastjson2UTF8Bytes millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.32.13 : 335
        }
    }

    public static void fastjson2JSONB_symbols() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2JSONB_symbols(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayWriteBinaryAutoType-fastjson2JSONB_symbols millis : " + millis);
            // zulu8.58.0.13 : 388 324 297 292
            // zulu11.52.13 :
            // zulu17.32.13 : 339
        }
    }

    public static void fastjson2JSONB() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2JSONB(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayWriteBinaryAutoType-fastjson2_jsonb millis : " + millis);
            // zulu8.62.0.19 : 414 405 308
            // zulu11.52.13 : 342
            // zulu17.32.13 : 358
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2JSONB();
        fastjson2JSONB_symbols();
//        fastjson2JSONB_ArrayMapping();
//        fastjson2UTF8Bytes();
//        hessian();
    }
}
