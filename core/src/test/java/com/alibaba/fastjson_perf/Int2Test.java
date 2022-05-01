package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2_vo.Int2;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class Int2Test {
    private String str;

    public Int2Test() throws Exception {
        InputStream is = Int2Test.class.getClassLoader().getResourceAsStream("data/Int2.json");
        str = IOUtils.toString(is, "UTF-8");
    }


    @Test
    public void test_invoke_createObjectConsumer() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10; ++j) {
                ObjectReaders.of(Int2.class);
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
                ObjectReaders.ofReflect(Int2.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("relfect millis : " + millis);
        }
        System.out.println();
    }

    @Test
    public void test_reflect_parse() {
        ObjectReader<Int2> objectConsumer = ObjectReaders.ofReflect(Int2.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int2 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Int2 relfect millis : " + millis);
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_invoke_parse() throws Throwable {
        ObjectReader<Int2> objectConsumer = ObjectReaders.of(Int2.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int2 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Int2 invoke millis : " + millis);
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }


    @Test
    public void test_asm_parse() throws Throwable {
        ObjectReader<Int2> objectConsumer = TestUtils.of(Int2.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int2 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Int2 asm millis : " + millis);
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }
}
