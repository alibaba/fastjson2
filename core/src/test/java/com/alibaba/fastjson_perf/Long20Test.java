package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2_vo.Long20;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class Long20Test {
    private String str;

    public Long20Test() throws Exception {
        InputStream is = Long20Test.class.getClassLoader().getResourceAsStream("data/Long20.json");
        str = IOUtils.toString(is, "UTF-8");
    }

    @Test
    public void test_invoke_createObjectConsumer() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10; ++j) {
                ObjectReaders.of(Long20.class);
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
                ObjectReaders.ofReflect(Long20.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("relfect millis : " + millis);
        }
        System.out.println();
    }

    @Test
    public void test_reflect_parse() {
        ObjectReader<Long20> objectConsumer = ObjectReaders.ofReflect(Long20.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Long20 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Long20 relfect millis : " + millis); // 2191 2109
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_invoke_parse() throws Throwable {
        ObjectReader<Long20> objectConsumer = ObjectReaders.of(Long20.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Long20 vo = objectConsumer.readObject(parser, 0);;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Long20 invoke millis : " + millis); // 1762
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }


    @Test
    public void test_asm_parse() throws Throwable {
        ObjectReader<Long20> objectConsumer = TestUtils.of(Long20.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Long20 vo = objectConsumer.readObject(parser, 0);;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Long20 asm millis : " + millis); // 1613
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }
}
