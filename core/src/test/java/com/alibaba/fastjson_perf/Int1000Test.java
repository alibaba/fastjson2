package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2_vo.Int1000;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class Int1000Test {
    private String str;

    public Int1000Test() throws Exception {
        InputStream is = Int1000Test.class.getClassLoader().getResourceAsStream("data/Int1000.json");
        str = IOUtils.toString(is, "UTF-8");
    }


    @Test
    public void test_invoke_createObjectConsumer() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10; ++j) {
                ObjectReaders.of(Int1000.class);
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
                ObjectReaders.ofReflect(Int1000.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("relfect millis : " + millis);
        }
        System.out.println();
    }

    @Test
    public void test_reflect_parse() {
        ObjectReader<Int1000> objectConsumer = ObjectReaders.ofReflect(Int1000.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int1000 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Int1000 relfect millis : " + millis); // 1372
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_invoke_parse() throws Throwable {
        ObjectReader<Int1000> objectConsumer = ObjectReaders.of(Int1000.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int1000 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Int1000 invoke millis : " + millis); // 1089
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }


    @Test
    public void test_asm_parse() throws Throwable {
        ObjectReader<Int1000> objectConsumer = TestUtils.of(Int1000.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int1000 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Int1000 asm millis : " + millis); //  1529 996
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }
}
