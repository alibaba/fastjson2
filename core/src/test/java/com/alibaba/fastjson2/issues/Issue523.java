package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue523 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"type\":101}", Bean.class);
        assertEquals(101, bean.type);
    }

    public static class Bean {
        private Integer type;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public void setType(DeviceLogicEnum type) {
            this.type = type.key;
        }
    }

    @Test
    public void test1() {
        assertEquals(
                101,
                JSON.parseObject("{\"type\":101}", Bean1.class).type
        );
    }

    public static class Bean1 {
        private Integer type;

        public Integer getType() {
            return type;
        }

        public void setType(DeviceLogicEnum type) {
            this.type = type.key;
        }

        public void setType(Integer type) {
            this.type = type;
        }
    }

    @Test
    public void test2() {
        assertEquals(
                101,
                JSON.parseObject("{\"type\":101}", Bean2.class).type
        );
    }

    public static class Bean2 {
        private int type;

        public int getType() {
            return type;
        }

        public void setType(DeviceLogicEnum type) {
            this.type = type.key;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

    @Test
    public void test3() {
        assertEquals(
                101,
                JSON.parseObject("{\"type\":101}", Bean3.class).type
        );
    }

    public static class Bean3 {
        private int type;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setType(DeviceLogicEnum type) {
            this.type = type.key;
        }
    }

    public static enum DeviceLogicEnum {
        Big(1), Medium(2), Small(3);

        private final int key;

        DeviceLogicEnum(int key) {
            this.key = key;
        }
    }
}
