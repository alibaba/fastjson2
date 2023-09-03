package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayParseBinaryArrayMappingTest {
    static final int LOOP = 10_000_000;
    static final EishayParseBinaryArrayMapping benchmark = new EishayParseBinaryArrayMapping();

    public static void kryo() throws Exception {
        System.out.println("kryo size " + EishayParseBinaryArrayMapping.kryoBytes.length); // 213
        System.out.println();
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.kryo(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseBinaryArrayMapping-kryo millis : " + millis);
            // zulu8.58.0.13 : 4483
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void jsonb() throws Exception {
        System.out.println("EishayParseBinaryArrayMapping-jsonb size " + EishayParseBinaryArrayMapping.fastjson2JSONBBytes.length); // 223
        System.out.println();

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseBinaryArrayMapping-fastjson2_jsonb millis : " + millis);
            // zulu8.68.0.21 : 1873 1637
            // zulu11.52.13 :
            // zulu17.32.13 : 1300
        }
    }

    public static void protobuf() throws Exception {
        System.out.println("protobuf size " + EishayParseBinaryArrayMapping.protobufBytes.length); // 235
        System.out.println();

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.protobuf(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseBinaryArrayMapping-protobuf millis : " + millis);
            // zulu8.68.0.21 : 5211
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void fury() throws Exception {
        System.out.println("fury size " + EishayParseBinaryArrayMapping.furyBytes.length); // 235
        System.out.println();

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.protobuf(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseBinaryArrayMapping-protobuf millis : " + millis);
            // zulu8.68.0.21 : 4660
            // zulu11.52.13 : 4475
            // zulu17.32.13 : 4357
        }
    }

    public static void main(String[] args) throws Exception {
        jsonb();
//        fury();
//        kryo();
//        protobuf();
//        fastjson2UTF8Bytes();
    }
}
