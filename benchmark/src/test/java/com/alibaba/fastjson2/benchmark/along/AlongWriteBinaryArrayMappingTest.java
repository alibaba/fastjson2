package com.alibaba.fastjson2.benchmark.along;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class AlongWriteBinaryArrayMappingTest {
    static final AlongWriteBinaryArrayMapping benchmark = new AlongWriteBinaryArrayMapping();
    static final int LOOP_COUNT = 10_000_000;

    public static void jsonb() throws Exception {
        // 255
        System.out.println("AlongWriteBinaryArrayMapping-jsonb size : " + benchmark.jsonbSize());

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("AlongWriteBinaryArrayMapping-fastjson2_jsonb millis : " + millis);
            // zulu8.68.0.21 :
            // zulu11.52.13 : 1477 1480 1470 1389 1361
            // zulu17.32.13 : 3126 2888 2736 2564 1674 1404
        }
    }

    public static void json() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; ++i) {
                benchmark.json(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("AlongWriteBinaryArrayMapping-fastjson2_jsonb millis : " + millis);
            // zulu8.68.0.21 :
            // zulu11.52.13 : 1046 1007 1004
            // zulu17.32.13 :
        }
    }

    public static void jsonStr() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; ++i) {
                benchmark.jsonStr(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("AlongWriteBinaryArrayMapping-fastjson2_jsonStr millis : " + millis);
            // zulu8.68.0.21 :
            // zulu11.52.13 : 1032
            // zulu17.32.13 :
        }
    }

    public static void fury() throws Exception {
        // 449
        System.out.println("AlongWriteBinaryArrayMapping-fury size : " + benchmark.furySize());

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; ++i) {
                benchmark.fury(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("AlongWriteBinaryArrayMapping-fury millis : " + millis);
            // size 379
            // zulu8.68.0.21 :
            // zulu11.52.13 : 2086 967
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
        jsonb();
//        json();
//        jsonStr();
//        fury();
    }
}
