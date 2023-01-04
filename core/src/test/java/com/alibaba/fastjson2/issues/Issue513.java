package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue513 {
    @Test
    public void test() {
        Set set = (Set) JSON.parse("Set[\"1541357098843803649\"]");
        assertEquals(1, set.size());
        assertTrue(set.contains("1541357098843803649"));
    }

    @Test
    public void test1() {
        Bean bean = JSON.parseObject("{\"values\":Set[\"1541357098843803649\"]}", Bean.class);
        Set set = (Set) bean.values;
        assertEquals(1, set.size());
        assertTrue(set.contains("1541357098843803649"));
    }

    public static class Bean {
        public Object values;
    }

    @Test
    public void test2() {
        Bean1 bean = JSON.parseObject("{\"values\":Set[\"1541357098843803649\"]}", Bean1.class);
        Set set = (Set) bean.values;
        assertEquals(1, set.size());
        assertTrue(set.contains("1541357098843803649"));
    }

    public static class Bean1 {
        public Collection values;
    }

    @Test
    public void test3() {
        Bean3 bean = JSON.parseObject("{\"values\":Set[\"1541357098843803649\"]}", Bean3.class);
        Set set = (Set) bean.values;
        assertEquals(1, set.size());
        assertTrue(set.contains("1541357098843803649"));
    }

    public static class Bean3 {
        private Collection values;

        public Collection getValues() {
            return values;
        }

        public void setValues(Collection values) {
            this.values = values;
        }
    }
}
