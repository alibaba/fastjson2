package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrivateBeanTest {
    @Test
    public void test() {
        String str = "{\"id\":123,\"name\":\"DataWorks\"}";
        Bean bean = JSON.parseObject(str, Bean.class);
        assertEquals(123, bean.id);
        assertEquals("DataWorks", bean.name);
    }

    public static class Bean {
        private int id;
        private String name;
        private Bean() {
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
