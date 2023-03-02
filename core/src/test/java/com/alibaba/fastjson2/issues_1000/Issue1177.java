package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
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
}
