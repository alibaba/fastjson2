package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1177 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.setName("wenshao");
        String str = JSON.toJSONString(bean);
        assertEquals("{\"age\":0,\"name\":\"wenshao\"}", str);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.name, bean1.name);
    }

    public static class Bean {
        private Function<String, String> func = str -> str;
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Function<String, String> getFunc() {
            return func;
        }

        public void setFunc(Function<String, String> func) {
            this.func = func;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        String str = JSON.toJSONString(bean);
        assertEquals("{}", str);
    }

    @Data
    public static class Bean1 {
        private MyFunction function = new MyFunction() {
            @Override
            public Object apply(Object o) {
                return null;
            }
        };
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        String str = JSON.toJSONString(bean);
        assertEquals("{}", str);
    }

    public static class Bean2 {
        public MyFunction function = new MyFunction() {
            @Override
            public Object apply(Object o) {
                return null;
            }
        };
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        String str = JSON.toJSONString(bean);
        assertEquals("{}", str);
    }

    private static class Bean3 {
        public MyFunction function = new MyFunction() {
            @Override
            public Object apply(Object o) {
                return null;
            }
        };
    }

    @FunctionalInterface
    public interface MyFunction<R, T> {
        R apply(T t);
    }
}
