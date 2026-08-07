package com.alibaba.fastjson2_perf.odps;

import com.alibaba.fastjson2.support.odps.JSONExtract;
import com.aliyun.odps.io.Text;
import com.aliyun.odps.io.Writable;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONExtractTest {
    private String str;
    private byte[] utf8Bytes;

    public JSONExtractTest() throws Exception {
        InputStream is = JSONExtractTest.class.getClassLoader().getResourceAsStream("data/path_01.json");
        str = IOUtils.toString(is, "UTF-8");
        utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void test_extract_id() {
        JSONExtract udf = new JSONExtract("$.id");
        {
            Text intput = new Text(utf8Bytes);
            Writable result = udf.eval(intput);
            assertEquals("\"1\"",
                    result.toString());
        }

        Text intput = new Text(utf8Bytes);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                Writable result = udf.eval(intput);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("extract : " + millis); // 410 387 346 317
        }
    }

    @Test
    public void test_extract_Duration() {
        JSONExtract udf = new JSONExtract("$.Duration");
        {
            Text intput = new Text(utf8Bytes);
            Writable result = udf.eval(intput);
            assertEquals("\"450\"",
                    result.toString());
        }

        Text intput = new Text(utf8Bytes);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                Writable result = udf.eval(intput);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("extract : " + millis); // 1272 1234 1077 1018
        }
    }

    @Test
    public void test_extract_StartTime() {
        JSONExtract udf = new JSONExtract("$.StartTime");
        {
            Text intput = new Text(utf8Bytes);
            Writable result = udf.eval(intput);
            assertEquals("\"2020-01-06 17:00:00\"",
                    result.toString());
        }

        Text intput = new Text(utf8Bytes);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                Writable result = udf.eval(intput);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("extract : " + millis); // 817 700
        }
    }
}
