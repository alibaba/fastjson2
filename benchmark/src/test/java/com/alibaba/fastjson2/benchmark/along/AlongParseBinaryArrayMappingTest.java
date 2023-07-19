package com.alibaba.fastjson2.benchmark.along;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class AlongParseBinaryArrayMappingTest {
    static final int LOOP = 10_000_000;
    static final AlongParseBinaryArrayMapping benchmark = new AlongParseBinaryArrayMapping();

    public static void jsonb() throws Exception {
        System.out.println("AlongParseBinaryArrayMapping-jsonb size " + AlongParseBinaryArrayMapping.fastjson2JSONBBytes.length); // 223
        System.out.println();

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("AlongParseBinaryArrayMapping-fastjson2_jsonb millis : " + millis);
            // zulu8.68.0.21 : 2928 2887 2826 2806 2754 2693 1876
            // zulu11.52.13 : 2484 2170 1916 1911 1892 1879 1876
            // zulu17.32.13 : 2601 2457 1691
        }
    }

    public static void main(String[] args) throws Exception {
        jsonb();
    }
}
