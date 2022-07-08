package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

public class JSONPathCompilerReflectTest {
    JSONPathCompilerReflect compiler = JSONPathCompilerReflect.INSTANCE;
    BiFunction increment = (object, value) -> ((Number) value).intValue() + 1;

    @Test
    public void test() {
        JSONPath path = compiler.compile(
                Bean.class,
                JSONPath.of("$.id")
        );
        assertTrue(path.isRef());

        Bean bean = new Bean();
        bean.id = 100;

        assertTrue(path.contains(bean));

        path.setCallback(bean, increment);
        assertEquals(101, bean.id);

        path.set(bean, 200);
        assertEquals(200, bean.id);
        assertEquals(200, path.eval(bean));
        path.set(bean, 201, JSONReader.Feature.InitStringFieldAsEmpty);
        path.setLong(bean, 202);
        assertEquals(202, path.eval(bean));

        assertThrows(Exception.class, () -> path.remove(bean));
        assertThrows(Exception.class, () -> path.extract(JSONReader.of("")));
        assertThrows(Exception.class, () -> path.extractScalar(JSONReader.of("")));
    }

    @Test
    public void test1() {
        JSONPath path = compiler.compile(
                Bean.class,
                JSONPath.of("$.item.id")
        );

        Bean bean = new Bean();
        bean.item = new Item();
        bean.item.id = 1000;

        path.setCallback(bean, increment);

        assertEquals(1001, bean.item.id);
        path.setLong(bean, 1002);
        assertEquals(1002, bean.item.id);
        path.set(bean, 1003);
        assertEquals(1003, bean.item.id);

        bean.item = null;
        path.setInt(bean, 1002);
        path.setLong(bean, 1002);
        path.set(bean, 1003);
        assertNull(path.eval(bean));
    }

    @Test
    public void test2() {
        JSONPath path = compiler.compile(
                Bean.class,
                JSONPath.of("$.item.child.id")
        );

        Bean bean = new Bean();
        bean.item = new Item();
        bean.item.child = new Child();
        bean.item.child.id = 100;
        path.setCallback(bean, increment);
        assertEquals(101, path.eval(bean));
    }

    public static class Bean {
        public int id;

        public Item item;
    }

    public static class Item {
        public long id;

        public Child child;
    }

    public static class Child {
        public int id;
    }
}
