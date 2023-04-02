package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FactoryFunctionTest {
    @Test
    public void test() {
        assertEquals(
                123,
                JSON.parseObject("{\"id\":123}", Bean1.class).id
        );

        assertEquals(
                123,
                JSON.parseObject("{\"id\":\"123\"}", Bean1.class).id
        );

        assertEquals(
                123,
                JSON.parseObject("{\"id\":123}").to(Bean1.class).id
        );

        assertEquals(
                123,
                JSON.parseObject("{\"id\":\"123\"}").to(Bean1.class).id
        );
    }

    public static class Bean1 {
        private int id;

        private Bean1(int id) {
            this.id = id;
        }

        @JSONCreator
        public static Bean1 create(int id) {
            return new Bean1(id);
        }
    }

    @Test
    public void test2() {
        assertEquals(
                123,
                JSON.parseObject("{\"id\":123,\"name\":\"abc\"}", Bean2.class).id
        );

        assertEquals(
                123,
                JSON.parseObject("{\"id\":\"123\",\"name\":\"abc\"}", Bean2.class).id
        );

        assertEquals(
                123,
                JSON.parseObject("{\"id\":123,\"name\":\"abc\"}").to(Bean2.class).id
        );

        assertEquals(
                123,
                JSON.parseObject("{\"id\":\"123\",\"name\":\"abc\"}").to(Bean2.class).id
        );
        assertEquals(
                "abc",
                JSON.parseObject("{\"id\":\"123\",\"name\":\"abc\"}").to(Bean2.class).name
        );
    }

    public static class Bean2 {
        private int id;
        private String name;

        private Bean2(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @JSONCreator
        public static Bean2 create(int id, String name) {
            return new Bean2(id, name);
        }
    }

    @Test
    public void test3() {
        Bean3 bean = JSON.parseObject("{\"id\":123,\"name\":\"abc\",\"description\":\"ddd\"}", Bean3.class);
        assertEquals(123, bean.id);
        assertEquals("abc", bean.name);
        assertEquals("ddd", bean.description);
    }

    public static class Bean3 {
        private int id;
        private String name;
        private String description;

        private Bean3(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        @JSONCreator
        public static Bean3 create(int id, String name, String description) {
            return new Bean3(id, name, description);
        }
    }

    @Test
    public void test4Error() {
        assertThrows(
                Exception.class,
                () -> JSON.parseObject("{\"id\":123,\"name\":\"abc\",\"description\":\"ddd\"}", BeanError.class)
        );
    }

    public static class BeanError {
        private int id;
        private String name;
        private String description;

        private BeanError(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        @JSONCreator
        public static BeanError create(int id, String name, String description) {
            throw new UnsupportedOperationException();
        }
    }
}
