package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayWriteBinaryArrayMappingTest {
    static final EishayWriteBinaryArrayMapping benchmark = new EishayWriteBinaryArrayMapping();

    public static void kryo() throws Exception {
        System.out.println("kryoSize size " + benchmark.kryoSize()); // 213

        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.kryo(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayWriteBinaryArrayMapping-kryo millis : " + millis);
            // zulu8.62.0.19 : 396
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void jsonb() throws Exception {
        System.out.println("protobuf size " + benchmark.jsonbSize()); // 235

        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayWriteBinaryArrayMapping-jsonb millis : " + millis);
            // zulu8.62.0.19 : 190 168
            // zulu11.52.13 : 105
            // zulu17.32.13 : 105
        }
    }

    public static void protobuf() throws Exception {
        System.out.println("protobuf size " + benchmark.protobufSize()); // 235

        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.protobuf(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayWriteBinaryArrayMapping-protobuf millis : " + millis);
            // zulu8.58.0.13 :
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
//        jsonb();
        kryo();
//        protobuf();
    }
}
