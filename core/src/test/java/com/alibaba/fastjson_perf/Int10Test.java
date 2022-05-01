package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2_vo.Int10;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class Int10Test {
    private String str;

    public Int10Test() throws Exception {
        InputStream is = Int10Test.class.getClassLoader().getResourceAsStream("data/Int10.json");
        str = IOUtils.toString(is, "UTF-8");
    }


    @Test
    public void test_invoke_createObjectConsumer() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10; ++j) {
                ObjectReaders.of(Int10.class);
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
                ObjectReaders.ofReflect(Int10.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("relfect millis : " + millis);
        }
        System.out.println();
    }

    @Test
    public void test_reflect_parse() {
        ObjectReader<Int10> objectConsumer = ObjectReaders.ofReflect(Int10.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int10 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Int10 relfect millis : " + millis); // 488
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_invoke_parse() throws Throwable {
        ObjectReader<Int10> objectConsumer = ObjectReaders.of(Int10.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int10 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Int10 invoke millis : " + millis); // 463 505
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }


    @Test
    public void test_asm_parse() throws Throwable {
        ObjectReader<Int10> objectConsumer = TestUtils.of(Int10.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int10 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Int10 asm millis : " + millis); // 555 541 528 527 408
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }
}
