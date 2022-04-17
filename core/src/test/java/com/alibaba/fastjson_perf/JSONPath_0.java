package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONPath;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static junit.framework.TestCase.assertEquals;

public class JSONPath_0 {
    private String str;
    private byte[] utf8Bytes;

    public JSONPath_0() throws Exception {
        InputStream is = Int100Test.class.getClassLoader().getResourceAsStream("data/path_01.json");
        str = IOUtils.toString(is, "UTF-8");
        utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void perf_extract_id() throws Exception {
        JSONPath path = JSONPath.of("$.id");
        {
            JSONReader parser = JSONReader.of(str);
            assertEquals(1, path.extract(parser));
        }


        char[] chars = str.toCharArray();

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                JSONReader parser = JSONReader.of(chars);
                path.extract(parser);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println(millis); // 447 438 361
        }
    }

    @Test
    public void perf_extractInt32_id() throws Exception {
        JSONPath path = JSONPath.of("$.id");
        {
            JSONReader parser = JSONReader.of(str);
            assertEquals(1L, path.extractInt64(parser).longValue());
        }

        char[] chars = str.toCharArray();

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                JSONReader parser = JSONReader.of(chars);
                path.extractInt32(parser);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println(millis); // 337
        }
    }

    @Test
    public void perf_extractInt64_id() throws Exception {
        JSONPath path = JSONPath.of("$.id");
        {
            JSONReader parser = JSONReader.of(str);
            assertEquals(1L, path.extractInt64(parser).longValue());
        }


        char[] chars = str.toCharArray();

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                JSONReader parser = JSONReader.of(chars);
                path.extractInt64(parser);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println(millis); // 275
        }
    }

    @Test
    public void perf_extractInt64_id_utf8() throws Exception {
        JSONPath path = JSONPath.of("$.id");
        {
            JSONReader parser = JSONReader.of(str);
            assertEquals(1L, path.extractInt64(parser).longValue());
        }

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                JSONReader parser = JSONReader.of(utf8Bytes);
                path.extractInt64(parser);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println(millis); // 292
        }
    }

    @Test
    public void perf_extractInt64_Duration() throws Exception {
        JSONPath path = JSONPath.of("$.Duration");
        {
            JSONReader parser = JSONReader.of(str);
            assertEquals(450L, path.extractInt64(parser).longValue());
        }

        char[] chars = str.toCharArray();

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                JSONReader parser = JSONReader.of(chars);
                path.extractInt64Value(parser);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println(millis); // 996 908 900 897
        }
    }

    @Test
    public void perf_extractInt64_Duration_utf8() throws Exception {
        JSONPath path = JSONPath.of("$.Duration");
        {
            JSONReader parser = JSONReader.of(utf8Bytes);
            assertEquals(450L, path.extractInt64(parser).longValue());
        }

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                JSONReader parser = JSONReader.of(utf8Bytes);
                path.extractInt64Value(parser);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println(millis); // 2089 1677 1430 1423 1402 1077 983 1095
        }
    }

    @Test
    public void perf_extractInt64_user_agent_resolution_utf8() throws Exception {
        JSONPath path = JSONPath.of("$.user_agent.resolution");
        {
            JSONReader parser = JSONReader.of(utf8Bytes);
            assertEquals("1024x4069", path.extractScalar(parser));
        }

        for (int i = 0; i < 5; ++i) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                JSONReader parser = JSONReader.of(utf8Bytes);
                path.extractScalar(parser);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println(millis); // 3479
        }
    }

    @Test
    public void perf_extractInt64_user_events_1_EventId_utf8() throws Exception {
        JSONPath path = JSONPath.of("$.events[1].EventId");
        {
            JSONReader parser = JSONReader.of(utf8Bytes);
            assertEquals(548, path.extractInt32(parser).intValue());
        }

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 1000 * 1000 * 1; ++j) {
                JSONReader parser = JSONReader.of(utf8Bytes);
                path.extractInt32(parser);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println(millis); // 672 618 585 576 557
        }
    }

    @Test
    public void perf_extractInt64_user_events_1_EventProperties_items_utf8() throws Exception {
        JSONPath path = JSONPath.of("$.events[1].EventProperties.items");
        {
            JSONReader parser = JSONReader.of(utf8Bytes);
            assertEquals(2, path.extractInt32(parser).intValue());
        }

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 1000 * 1000 * 1; ++j) {
                JSONReader parser = JSONReader.of(utf8Bytes);
                path.extractInt32(parser);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println(millis); // 752
        }
    }

    public static void main(String[] args) throws Exception {
//        JSONPath_0 test = new JSONPath_0();
//        test.perf_extractInt64_Duration_utf8();

        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; ++i) {
            int x = i & 0xff;
            System.out.println((x >> 4) + "\t" + x + "\t" + i);
        }
    }
}
