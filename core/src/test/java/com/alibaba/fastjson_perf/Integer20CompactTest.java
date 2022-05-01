package com.alibaba.fastjson_perf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2_vo.Integer20;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Integer20CompactTest {
    private String str;

    public Integer20CompactTest() throws Exception {
        InputStream is = Integer20CompactTest.class.getClassLoader().getResourceAsStream("data/Int20_compact.json");
        str = IOUtils.toString(is, "UTF-8");
    }

    @Test
    public void compact() {
        System.out.println(JSON.toJSONString(
                JSON.parseObject(str, Integer20.class))
        );
    }

    @Test
    public void test_invoke_createObjectConsumer() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10; ++j) {
                ObjectReaders.of(Integer20.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("invoke millis : " + millis);
        }
        System.out.println();
    }

    @Test
    public void test_reflect_createObjectConsumer() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 100; ++j) {
                ObjectReaders.ofReflect(Integer20.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("relfect millis : " + millis);
        }
        System.out.println();
    }

    @Test
    public void test_reflect_parse() {
        ObjectReader<Integer20> objectConsumer = ObjectReaders.ofReflect(Integer20.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Integer20 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Integer20 relfect millis : " + millis); // 776
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_invoke_parse() throws Throwable {
        ObjectReader<Integer20> objectConsumer = ObjectReaders.of(Integer20.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Integer20 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Integer20 invoke millis : " + millis); // 880 840 802
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }


    @Test
    public void test_asm_parse() throws Throwable {
        ObjectReader<Integer20> objectConsumer = TestUtils.of(Integer20.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Integer20 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Integer20 asm millis : " + millis); // 885 836 825 682 654
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_f12_parse() throws Throwable {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                Integer20 vo =JSON.parseObject(str, Integer20.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Integer20 f12 millis : " + millis); // 675
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_asm_parse_utf8_bytes() throws Throwable {
        ObjectReader<Integer20> objectConsumer = TestUtils.of(Integer20.class);

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(bytes);
                Integer20 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Integer20 asm millis : " + millis); //
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }


    @Test
    public void test_asm_parse_ascii_bytes() throws Throwable {
        ObjectReader<Integer20> objectConsumer = TestUtils.of(Integer20.class);

        byte[] bytes = str.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII);
                Integer20 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Integer20 asm millis : " + millis); //
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }
}
