package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2_vo.Int100;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class Int100Test {
    private String str;

    public Int100Test() throws Exception {
        InputStream is = Int100Test.class.getClassLoader().getResourceAsStream("data/Int100.json");
        str = IOUtils.toString(is, "UTF-8");
    }


    @Test
    public void test_invoke_createObjectConsumer() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10; ++j) {
                ObjectReaders.of(Int100.class);
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

            for (int j = 0; j < 100000; ++j) {
                ObjectReaders.ofReflect(Int100.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("relfect millis : " + millis); // 992
        }
        System.out.println();
    }

    @Test
    public void test_reflect_parse() {
        ObjectReader<Int100> objectConsumer = ObjectReaders.ofReflect(Int100.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 100000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int100 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("relfect millis : " + millis); // 577 511
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_invoke_parse() throws Throwable {
        ObjectReader<Int100> objectConsumer = ObjectReaders.of(Int100.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 100000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int100 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("invoke millis : " + millis); // 477 411
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_asm_parse() throws Throwable {
        ObjectReader<Int100> objectConsumer = TestUtils.of(Int100.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 100000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int100 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("asm millis : " + millis); // 315 309
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }
}
