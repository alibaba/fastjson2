package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
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

    @Test
    public void test6() {
        String str = "123";
        Bean6 bean = JSON.parseObject(str, Bean6.class);
        assertEquals(123, bean.value);
    }

    public static class Bean6 {
        private final int value;

        @JSONCreator
        public Bean6(@JSONField(value = true) int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @Test
    public void test7() {
        String str = "123";
        Bean7 bean = JSON.parseObject(str, Bean7.class);
        assertEquals(123, bean.value);
    }

    public static class Bean7 {
        private final int value;

        private Bean7(int value) {
            this.value = value;
        }

        @JSONCreator
        public static Bean7 of(@JSONField(value = true) int value) {
            return new Bean7(value);
        }

        public int getValue() {
            return value;
        }
    }

    @Test
    public void test8() {
        String str = "123";
        Bean8 bean = JSON.parseObject(str, Bean8.class);
        assertEquals("123", bean.value);
    }

    public static class Bean8 {
        private final String value;

        @JSONCreator
        public Bean8(@JSONField(value = true) String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Test
    public void test9() {
        String str = "123";
        Bean9 bean = JSON.parseObject(str, Bean9.class);
        assertEquals("123", bean.value);
    }

    public static class Bean9 {
        private final String value;

        private Bean9(String value) {
            this.value = value;
        }

        @JSONCreator
        public static Bean9 of(@JSONField(value = true) String value) {
            return new Bean9(value);
        }

        public String getValue() {
            return value;
        }
    }

    @Test
    public void test10() {
        String str = "123";
        Bean10 bean = JSON.parseObject(str, Bean10.class);
        assertEquals(123, bean.value);
    }

    public static class Bean10 {
        private final Integer value;

        @JSONCreator
        public Bean10(@JSONField(value = true) Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }

    @Test
    public void test11() {
        String str = "123";
        Bean11 bean = JSON.parseObject(str, Bean11.class);
        assertEquals(123, bean.value);
    }

    public static class Bean11 {
        private final Integer value;

        private Bean11(Integer value) {
            this.value = value;
        }

        @JSONCreator
        public static Bean11 of(@JSONField(value = true) Integer value) {
            return new Bean11(value);
        }

        public Integer getValue() {
            return value;
        }
    }
}
