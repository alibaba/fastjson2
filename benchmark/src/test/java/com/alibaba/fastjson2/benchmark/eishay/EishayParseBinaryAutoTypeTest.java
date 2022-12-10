package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayParseBinaryAutoTypeTest {
    static final EishayParseBinaryAutoType benchmark = new EishayParseBinaryAutoType();

    public static void fastjson2JSONB() throws Exception {
        System.out.println("EishayParseBinaryAutoType-fastjson2_jsonb size " + benchmark.fastjson2JSONBBytes.length); // 409
        System.out.println();

        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2JSONB(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseBinaryAutoType-fastjson2_jsonb millis : " + millis);
            // zulu8.58.0.13 : 394
            // zulu11.52.13 : 336
            // zulu17.32.13 : 324 319 326
            // zulu18.28.13 : 321
            // zulu19.28.81 : 324

            // reflect-zulu17.32.13 : 406
        }
    }

    public static void fastjson2JSONB_autoTypeFilter() throws Exception {
        System.out.println("EishayParseBinaryAutoType-fastjson2_jsonb_autoTypeFilter size " + benchmark.fastjson2JSONBBytes.length); // 409
        System.out.println();

        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2JSONB_autoTypeFilter(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseBinaryAutoType-fastjson2_jsonb_autoTypeFilter millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.32.13 :

            // reflect-zulu17.32.13 :
        }
    }

    public static void fastjson2JSONB_symbols() throws Exception {
        System.out.println("EishayParseBinaryAutoType-fastjson2JSONB_symbols size " + benchmark.fastjson2JSONBBytes_symbols.length); // 409
        System.out.println();

//        JSONBDump.dump(benchmark.fastjson2JSONBBytes_symbols, benchmark.symbolTable);

        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2JSONB_symbols(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseBinaryAutoType-fastjson2JSONB_symbols millis : " + millis);
            // zulu8.58.0.13 : 305
            // zulu11.52.13 : 246
            // zulu17.32.13 : 251 248 245 243 238
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2JSONB();
//        fastjson2JSONB_autoTypeFilter();
        fastjson2JSONB_symbols();
//        kryo_test();
    }
}
