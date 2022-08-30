package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue707 {
    @Test
    public void test() {
        Bean bean = new Bean();
        String str = JSON.toJSONString(bean);
        assertEquals("{\"id\":0}", str);
    }

    public static class Bean {
        private int flags;
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            flags |= 1;
            this.id = id;
        }

        public boolean isSetId() {
            return (flags & 1) == 0;
        }

        public void unsetId() {
            flags = 0;
        }
    }
}
