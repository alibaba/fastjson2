package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompileTest2 {
    static Function increment = o -> ((Number) o).intValue() + 1;

    @Test
    public void test0() {
        JSONPath path = JSONPath.compile("$.item.id", Bean.class);

        Bean bean = new Bean();
        bean.item = new Item();

        path.setInt(bean, 101);
        assertEquals(101, bean.item.id);
        assertEquals(101, path.eval(bean));

        path.setCallback(bean, increment);
        assertEquals(102, bean.item.id);
        assertEquals(102, path.eval(bean));
    }

    @Test
    public void test0_1() {
        JSONPath path = JSONPath.compile("$.value.floor()", Bean.class);

        Bean bean = new Bean();
        bean.value = 2.5F;

        assertEquals(2.0D, path.eval(bean));
    }

    public static class Bean {
        private long id;
        public Item item;
        public float value;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }
    }

    public static class Item {
        public int id;
    }
}
