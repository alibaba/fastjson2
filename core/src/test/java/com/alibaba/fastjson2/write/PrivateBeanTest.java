package com.alibaba.fastjson2.write;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrivateBeanTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 101;
        bean.name = "DataWorks";

        assertEquals("{\"id\":101,\"name\":\"DataWorks\"}", JSON.toJSONString(bean));
    }

    private static class Bean {
        private int id;
        private String name;

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
