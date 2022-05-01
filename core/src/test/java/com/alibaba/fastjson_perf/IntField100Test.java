package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2_vo.Int100Field;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;

public class IntField100Test {
    private String str;

    public IntField100Test() throws Exception {
        InputStream is = IntField100Test.class.getClassLoader().getResourceAsStream("data/Int100.json");
        str = IOUtils.toString(is, "UTF-8");
    }

    @Test
    public void test_invoke_createObjectConsumer() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 10; ++j) {
                ObjectReaders.of(Int100Field.class);
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
                ObjectReaders.ofReflect(Int100Field.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("relfect millis : " + millis);
        }
        System.out.println();
    }

    @Test
    public void test_reflect_parse() {
        ObjectReader<Int100Field> objectConsumer = ObjectReaders.ofReflect(Int100Field.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 100000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int100Field vo = objectConsumer.readObject(parser, 0);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("relfect millis : " + millis); // 497 460
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_invoke_parse() throws Throwable {
        ObjectReader<Int100Field> objectConsumer = ObjectReaders.of(Int100Field.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 100000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int100Field vo = objectConsumer.readObject(parser, 0);;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("invoke millis : " + millis); // 457 417
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void test_asm_parse() throws Throwable {
        ObjectReader<Int100Field> objectConsumer = TestUtils.of(Int100Field.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 100000; ++j) {
                JSONReader parser = JSONReader.of(str);
                Int100Field vo = objectConsumer.readObject(parser, 0);;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("asm millis : " + millis); // 581 429
//            System.out.println(vo.getV0000());
        }
        System.out.println();
    }

    @Test
    public void f() throws Throwable {
        Field field = Int100Field.class.getField("v0000");
        lambdaSetter(field);
    }

    @Test
    public void f1() throws Throwable {
        Field field = VO.class.getField("value");
        lambdaSetter(field);
    }

    private static Object lambdaSetter(Class objectType, Class fieldClass, Field field) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            final MethodHandle setter = lookup.unreflectSetter(field);
            final CallSite site = LambdaMetafactory.metafactory(lookup,
                    "accept", MethodType.methodType(BiConsumer.class, MethodHandle.class),
                    setter.type().erase(), MethodHandles.exactInvoker(setter.type()), setter.type());
            return site.getTarget().invokeExact(setter);
        } catch (Throwable e) {
            throw new JSONException("create fieldReader error", e);
        }
    }

    public static BiConsumer lambdaSetter(Field field) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        final MethodHandle setter = lookup.unreflectSetter(field);
        final CallSite site = LambdaMetafactory.metafactory(lookup,
                "accept", MethodType.methodType(BiConsumer.class, MethodHandle.class),
                setter.type().erase(), MethodHandles.exactInvoker(setter.type()), setter.type());
        return (BiConsumer) site.getTarget().invokeExact(setter);
    }

    public static class VO {
        public Integer value;
    }
}
