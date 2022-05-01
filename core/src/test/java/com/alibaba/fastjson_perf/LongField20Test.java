package com.alibaba.fastjson_perf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2_vo.Long20Field;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LongField20Test {
    private String str;

    public LongField20Test() throws Exception {
        InputStream is = LongField20Test.class.getClassLoader().getResourceAsStream("data/Long20.json");
        str = IOUtils.toString(is, "UTF-8");
    }

    @Test
    public void test_invoke_createObjectConsumer() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10; ++j) {
                ObjectReaders.of(Long20Field.class);
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
                ObjectReaders.ofReflect(Long20Field.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("relfect millis : " + millis);
        }
        System.out.println();
    }

    @Test
    public void test_reflect_parse() {
        ObjectReader<Long20Field> objectConsumer = ObjectReaders.ofReflect(Long20Field.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Long20Field vo = objectConsumer.readObject(parser, 0);;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Long20 relfect millis : " + millis); // 1670
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_invoke_parse() throws Throwable {
        ObjectReader<Long20Field> objectConsumer = ObjectReaders.of(Long20Field.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Long20Field vo = objectConsumer.readObject(parser, 0);;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Long20 invoke millis : " + millis); // 1631
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }


    @Test
    public void test_asm_parse() throws Throwable {
        ObjectReader<Long20Field> objectConsumer = TestUtils.of(Long20Field.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Long20Field vo = objectConsumer.readObject(parser, 0);;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Long20 asm millis : " + millis); // 1447
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_f12_parse() throws Throwable {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                Long20Field vo =JSON.parseObject(str, Long20Field.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Long20 f12 millis : " + millis); // 4413
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_asm_parse_utf8_bytes() throws Throwable {
        ObjectReader<Long20Field> objectConsumer = TestUtils.of(Long20Field.class);

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(bytes);
                Long20Field vo = objectConsumer.readObject(parser, 0);;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Long20 asm millis : " + millis); // 1955
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }


    @Test
    public void test_asm_parse_ascii_bytes() throws Throwable {
        ObjectReader<Long20Field> objectConsumer = TestUtils.of(Long20Field.class);

        byte[] bytes = str.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII);
                Long20Field vo = objectConsumer.readObject(parser, 0);;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Long20 asm millis : " + millis); // 1720
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }
}
