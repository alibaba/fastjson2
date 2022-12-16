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

    @Test
    public void test1() {
        String str = "{\"id\":123,\"name\":\"DataWorks\"}";
        Bean1 bean = JSON.parseObject(str, Bean1.class);
        assertEquals(123, bean.id);
        assertEquals("DataWorks", bean.name);
    }

    private static class Bean1 {
        private int id;
        private String name;
        private Bean1() {
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

    @Test
    public void test2() {
        String str = "{\"id\":123,\"name\":\"DataWorks\"}";
        Bean2 bean = JSON.parseObject(str, Bean2.class);
        assertEquals(123, bean.id);
        assertEquals("DataWorks", bean.name);
    }

    private static class Bean2 {
        private int id;
        private String name;
        public Bean2() {
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

    @Test
    public void test3() {
        String str = "{\"id\":123,\"name\":\"DataWorks\"}";
        Bean3 bean = JSON.parseObject(str, Bean3.class);
        assertEquals(123, bean.id);
        assertEquals("DataWorks", bean.name);
    }

    public static class Bean3 {
        private int id;
        private String name;
        public Bean3() {
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

    @Test
    public void test4() {
        String str = "{\"id\":123,\"name\":\"DataWorks\"}";
        Bean4 bean = JSON.parseObject(str, Bean4.class);
        assertEquals(123, bean.id);
        assertEquals("DataWorks", bean.name);
    }

    private static class Bean4 {
        private int id;
        private String name;
        public Bean4() {
        }

        public int getId() {
            return id;
        }

        public Bean4 setId(int id) {
            this.id = id;
            return this;
        }

        public String getName() {
            return name;
        }

        public Bean4 setName(String name) {
            this.name = name;
            return this;
        }
    }

    @Test
    public void test5() {
        String str = "{\"id\":123,\"name\":\"DataWorks\"}";
        Bean5 bean = JSON.parseObject(str, Bean5.class);
        assertEquals(123, bean.id);
        assertEquals("DataWorks", bean.name);
    }

    public static class Bean5 {
        private int id;
        private String name;
        public Bean5() {
        }

        public int getId() {
            return id;
        }

        public Bean5 setId(int id) {
            this.id = id;
            return this;
        }

        public String getName() {
            return name;
        }

        public Bean5 setName(String name) {
            this.name = name;
            return this;
        }
    }
}
