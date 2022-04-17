package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.hsf.VeryComplexDO;
import org.junit.jupiter.api.Test;

public class HSFPerf {
    byte[] bytes;
    public HSFPerf() {
        VeryComplexDO vo = VeryComplexDO.getFixedComplexDO();
        bytes = JSONB.toBytes(vo, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName);

        JSONB.parseObject(bytes, VeryComplexDO.class);

        System.out.println(JSON.toJSONString(vo));
    }

    @Test
    public void test_perf_write() throws Exception {
        VeryComplexDO vo = VeryComplexDO.getFixedComplexDO();
        for (int i = 0; i < 5; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10_000_000; ++j) {
                JSONB.toBytes(vo, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write millis : " + millis);
            // jdk-11.0.13 2917 2590 2551 2468 2341
        }
        System.out.println();
    }

    @Test
    public void test_perf_read() throws Exception {
        for (int i = 0; i < 5; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10_000_000; ++j) {
                JSONB.parseObject(bytes, VeryComplexDO.class, JSONReader.Feature.SupportAutoType);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("read millis : " + millis);
            // jdk-11.0.13 2749 2431 2422
        }
        System.out.println();
    }


}
