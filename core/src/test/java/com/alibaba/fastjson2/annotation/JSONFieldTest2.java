package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONFieldTest2 {
    @Test
    public void test_alternateNames() {
        VO vo = JSON.parseObject("{\"id\":101}", VO.class);
        assertEquals(101, vo.id);

        VO vo2 = JSON.parseObject("{\"uid\":101}", VO.class);
        assertEquals(101, vo2.id);
    }

    @Test
    public void test_alternateNames_2() {
        VO2 vo = JSON.parseObject("{\"id\":101}", VO2.class);
        assertEquals(101, vo.id);

        VO2 vo2 = JSON.parseObject("{\"uid\":101}", VO2.class);
        assertEquals(101, vo2.id);
    }

    @Test
    public void test_alternateNames_3() {
        VO3 vo = JSON.parseObject("{\"id\":101}", VO3.class);
        assertEquals(101, vo.id);

        VO3 vo2 = JSON.parseObject("{\"uid\":101}", VO3.class);
        assertEquals(101, vo2.id);
    }

    public static class VO {
        @JSONField(alternateNames = "uid")
        public int id;
    }

    public static class VO2 {
        private int id;

        public int getId() {
            return id;
        }

        @JSONField(alternateNames = "uid")
        public void setId(int id) {
            this.id = id;
        }
    }

    public static class VO3 {
        private int id;

        @JSONCreator
        public VO3(@JSONField(name = "id", alternateNames = "uid") int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
