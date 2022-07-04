package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompileTest {
    static Function increment = o -> ((Number) o).intValue() + 1;

    @Test
    public void test() {
        JSONPath path = JSONPath.compile("$.id", Bean.class);

        Bean bean = new Bean();
        path.setInt(bean, 101);
        assertEquals(101, bean.id);
        assertEquals(101, path.eval(bean));

        path.setCallback(bean, increment);
        assertEquals(102, bean.id);
        assertEquals(102, path.eval(bean));

        path.setCallback(bean,
                o -> ((Integer) o).intValue() + 1
        );
        assertEquals(103, bean.id);
    }

    @Test
    public void test2() {
        JSONPath path = JSONPath.compile("$.item.id", Bean.class);

        Bean bean = new Bean();
        bean.item = new Item();

        path.setInt(bean, 101);
        assertEquals(101, bean.item.id);
        assertEquals(101, path.eval(bean));

        path.setCallback(bean, increment);
        assertEquals(102, bean.item.id);
        assertEquals(102, path.eval(bean));

        path.setCallback(bean, o -> ((Integer) o).intValue() + 1);
        assertEquals(103, bean.item.id);
    }

    public static class Bean {
        private int id;
        public Item item;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static class Item {
        public int id;
    }

    @Test
    public void test1() {
        JSONPath path = JSONPath.compile("$.id", Bean1.class);

        Bean1 bean = new Bean1();
        path.setInt(bean, 101);
        assertEquals(101, bean.id);
        assertEquals(101L, path.eval(bean));

        path.setCallback(bean, increment);
        assertEquals(102, bean.id);
        assertEquals(102L, path.eval(bean));
    }

    public static class Bean1 {
        public long id;
    }

    @Test
    public void test3() {
        JSONPath path = JSONPath.compile("$.id", Bean3.class);

        Bean3 bean = new Bean3();
        path.setInt(bean, 101);
        assertEquals(101, bean.id);
        assertEquals(101F, path.eval(bean));

        path.setCallback(bean, increment);
        assertEquals(102, bean.id);
        assertEquals(102F, path.eval(bean));
    }

    public static class Bean3 {
        public float id;
    }

    @Test
    public void test4() {
        JSONPath path = JSONPath.compile("$.id", Bean4.class);

        Bean4 bean = new Bean4();
        path.setInt(bean, 101);
        assertEquals(101, bean.id);
        assertEquals(101D, path.eval(bean));

        path.setCallback(bean, increment);
        assertEquals(102, bean.id);
        assertEquals(102D, path.eval(bean));
    }

    public static class Bean4 {
        public double id;
    }

    @Test
    public void test5() {
        JSONPath path = JSONPath.compile("$.id", Bean5.class);

        Bean5 bean = new Bean5();
        path.setInt(bean, 101);
        assertEquals(101, bean.id);
        assertEquals((short) 101, path.eval(bean));

        path.setCallback(bean, increment);
        assertEquals(102, bean.id);
        assertEquals((short) 102, path.eval(bean));
    }

    public static class Bean5 {
        public short id;
    }

    @Test
    public void test6() {
        JSONPath path = JSONPath.compile("$.id", Bean6.class);

        Bean6 bean = new Bean6();
        path.setInt(bean, 101);
        assertEquals(101, bean.id);
        assertEquals((byte) 101, path.eval(bean));

        path.setCallback(bean, increment);
        assertEquals(102, bean.id);
        assertEquals((byte) 102, path.eval(bean));
    }

    public static class Bean6 {
        public byte id;
    }

    @Test
    public void test7() {
        JSONPath path = JSONPath.compile("$.id", Bean7.class);

        Bean7 bean = new Bean7();
        path.set(bean, true);
        assertEquals(true, bean.id);
        assertEquals(true, path.eval(bean));
    }

    public static class Bean7 {
        public boolean id;
    }

    @Test
    public void test8() {
        JSONPath path = JSONPath.compile("$.id", Bean8.class);

        Bean8 bean = new Bean8();
        path.set(bean, '8');
        assertEquals('8', bean.id);
        assertEquals('8', path.eval(bean));
    }

    public static class Bean8 {
        public char id;
    }
}
