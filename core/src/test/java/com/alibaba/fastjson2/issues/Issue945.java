package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.beans.Transient;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue945 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 10;
        bean.id2 = 102;

        assertEquals("{\"id\":10}", JSON.toJSONString(bean));
    }

    public static class Bean {
        public int id;
        public transient int id2;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.id = 10;
        bean.id2 = 102;

        assertEquals("{\"id\":10}", JSON.toJSONString(bean));
    }

    public static class Bean1 {
        private int id;

        private int id2;

        @Transient
        public int getId2() {
            return id2;
        }

        public void setId2(int id2) {
            this.id2 = id2;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
