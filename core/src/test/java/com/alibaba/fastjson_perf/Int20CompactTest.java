package com.alibaba.fastjson_perf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2_vo.Int20;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class Int20CompactTest {
    private String str;

    public Int20CompactTest() throws Exception {
        InputStream is = Int20CompactTest.class.getClassLoader().getResourceAsStream("data/Int20_compact.json");
        str = IOUtils.toString(is, "UTF-8");
    }


    @Test
    public void test_invoke_createObjectConsumer() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10; ++j) {
                ObjectReaders.of(Int20.class);
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
                ObjectReaders.ofReflect(Int20.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("relfect millis : " + millis);
        }
        System.out.println();
    }

    @Test
    public void test_reflect_parse() {
        ObjectReader<Int20> objectConsumer = ObjectReaders.ofReflect(Int20.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int20 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Int20 relfect millis : " + millis); // 975 874
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_invoke_parse() throws Throwable {
        ObjectReader<Int20> objectConsumer = ObjectReaders.of(Int20.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int20 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Int20 invoke millis : " + millis); // 774
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }


    @Test
    public void test_asm_parse() throws Throwable {
        ObjectReader<Int20> objectConsumer = TestUtils.of(Int20.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int20 vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Int20 asm millis : " + millis); // 731 693 681 643
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_f12_parse() throws Throwable {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                Int20 vo = JSON.parseObject(str, Int20.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Integer20 f12 millis : " + millis); // 625
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

//    @Test
//    public void test_unsafe_parse() throws Throwable {
//        ObjectReader<Int20> objectConsumer = ObjectReaderCreatorUnsafe.INSTANCE.createObjectReader(Int20.class);
//
//        for (int i = 0; i < 10; ++i) {
//            long start = System.currentTimeMillis();
//
//            for (int j = 0; j < 1000 * 1000; ++j) {
//                JSONReader parser = JSONReader.of(str);
//                Int20 vo = objectConsumer.readObject(parser, null);
//            }
//
//            long millis = System.currentTimeMillis() - start;
//            System.out.println("Int20 unsafe millis : " + millis); // 1362 1308
////            System.out.println(vo.getV0000());
//        }
//        System.out.println();
//    }
}
