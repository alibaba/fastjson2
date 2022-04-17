package com.alibaba.fastjson_perf;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.hsf.ComponentProtocol;
import com.alibaba.fastjson2.hsf.Result;
import org.junit.jupiter.api.Test;

public class HSFPerf2 {
    byte[] bytes;
    Result result;
    public HSFPerf2() {
        result = new Result();
        ComponentProtocol protocol = new ComponentProtocol();
        protocol.setEndpoint(new JSONObject()
                .fluentPut("id", "10123456790ABCDEFG1234590")
                .fluentPut("name", "中文")
        );
        protocol.setData(protocol.getEndpoint());
        result.setModel(protocol);

        bytes = JSONB.toBytes(result, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName);

        JSONB.parseObject(bytes, Result.class);

        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void test_perf_write() throws Exception {
        for (int i = 0; i < 5; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10_000_000; ++j) {
                JSONB.toBytes(result, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName);
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
                JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("read millis : " + millis);
            // jdk-11.0.13 2749 2431 2422
        }
        System.out.println();
    }


}
