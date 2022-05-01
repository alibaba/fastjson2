package com.alibaba.fastjson_perf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2_vo.StringField20;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static junit.framework.TestCase.assertNull;

public class StringField20NullsTest {
    private String str;

    public StringField20NullsTest() throws Exception {
        InputStream is = StringField20NullsTest.class.getClassLoader().getResourceAsStream("data/String20_null.json");
        str = IOUtils.toString(is, "UTF-8");
    }

    @Test
    public void test_invoke_createObjectConsumer() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10; ++j) {
                ObjectReaders.of(StringField20.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("StringField20 invoke millis : " + millis);
        }
        System.out.println();
    }

    @Test
    public void test_reflect_createObjectConsumer() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 100; ++j) {
                ObjectReaders.ofReflect(StringField20.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("StringField20 relfect millis : " + millis);
        }
        System.out.println();
    }

    @Test
    public void test_reflect_parse() {
        ObjectReader<StringField20> objectConsumer = ObjectReaders.ofReflect(StringField20.class);
        {
            JSONReader parser = JSONReader.of(str);
            StringField20 vo = objectConsumer.readObject(parser, 0);;
            assertNull(vo.v0000);
            assertNull(vo.v0001);
            assertNull(vo.v0002);
        }

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                StringField20 vo = objectConsumer.readObject(parser, 0);;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("StringField20 relfect millis : " + millis); // 589
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_invoke_parse() throws Throwable {
        ObjectReader<StringField20> objectConsumer = ObjectReaders.of(StringField20.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                StringField20 vo = objectConsumer.readObject(parser, 0);;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("StringField20 invoke millis : " + millis); // 618
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }


    @Test
    public void test_asm_parse() throws Throwable {
        ObjectReader<StringField20> objectConsumer = TestUtils.of(StringField20.class);
        {
            JSONReader parser = JSONReader.of(str);
            StringField20 vo = objectConsumer.readObject(parser, 0);;
            assertNull(vo.v0000);
            assertNull(vo.v0001);
            assertNull(vo.v0002);
        }

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(str);
                StringField20 vo = objectConsumer.readObject(parser, 0);;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("StringField20 asm millis : " + millis); // 617 532
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_f12_parse() throws Throwable {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                StringField20 vo = JSON.parseObject(str, StringField20.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("StringField20 f12 millis : " + millis); //
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_asm_parse_utf8_bytes() throws Throwable {
        ObjectReader<StringField20> objectConsumer = TestUtils.of(StringField20.class);

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(bytes);
                StringField20 vo = objectConsumer.readObject(parser, 0);;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("StringField20 asm millis : " + millis); //
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }


    @Test
    public void test_asm_parse_ascii_bytes() throws Throwable {
        ObjectReader<StringField20> objectConsumer = TestUtils.of(StringField20.class);

        byte[] bytes = str.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader parser = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII);
                StringField20 vo = objectConsumer.readObject(parser, 0);;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Long20 asm millis : " + millis); // 1720
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }
}
