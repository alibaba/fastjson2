package com.alibaba.fastjson2_perf.odps;

import com.alibaba.fastjson2.support.odps.JSONExtractScalar;
import com.aliyun.odps.io.Text;
import com.aliyun.odps.io.Writable;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONExtractScalarTest {
    private String str;
    private byte[] utf8Bytes;

    public JSONExtractScalarTest() throws Exception {
        InputStream is = JSONExtractScalarTest.class.getClassLoader().getResourceAsStream("data/path_01.json");
        str = IOUtils.toString(is, "UTF-8");
        utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void test_extract_id() {
        JSONExtractScalar udf = new JSONExtractScalar("$.id");
        {
            Text intput = new Text(utf8Bytes);
            Writable result = udf.eval(intput);
            assertEquals("1",
                    result.toString());
        }

        Text intput = new Text(utf8Bytes);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                Writable result = udf.eval(intput);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("extract : " + millis); // 500 449 414 395
        }
    }

    @Test
    public void test_extract_Duration() {
        JSONExtractScalar udf = new JSONExtractScalar("$.Duration");
        {
            Text intput = new Text(utf8Bytes);
            Writable result = udf.eval(intput);
            assertEquals("450",
                    result.toString());
        }

        Text intput = new Text(utf8Bytes);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                Writable result = udf.eval(intput);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("extract : " + millis); // 965
        }
    }

    @Test
    public void test_extract_StartTime() {
        JSONExtractScalar udf = new JSONExtractScalar("$.StartTime");
        {
            Text intput = new Text(utf8Bytes);
            Writable result = udf.eval(intput);
            assertEquals("2020-01-06 17:00:00",
                    result.toString());
        }

        Text intput = new Text(utf8Bytes);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                Writable result = udf.eval(intput);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("extract : " + millis); // 805 723 710
        }
    }
}
