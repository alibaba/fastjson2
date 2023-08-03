package com.alibaba.fastjson2.benchmark.along;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class AlongWriteBinaryArrayMappingTest {
    static final AlongWriteBinaryArrayMapping benchmark = new AlongWriteBinaryArrayMapping();

    public static void jsonb() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10_000_000; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("AlongWriteBinaryArrayMapping-fastjson2_jsonb millis : " + millis);
            // zulu8.68.0.21 :
            // zulu11.52.13 : 1477 1480 1470 1389 1361
            // zulu17.32.13 : 3126 2888 2736 2564 1674
        }
    }

    public static void json() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1_000_000; ++i) {
                benchmark.json(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("AlongWriteBinaryArrayMapping-fastjson2_jsonb millis : " + millis);
            // zulu8.68.0.21 :
            // zulu11.52.13 : 1046 1007
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
//        jsonb();
        json();
    }
}
