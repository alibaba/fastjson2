package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3434 {
    @Test
    public void test() {
        Bean bean = new Bean();
        String str = JSON.toJSONString(bean);
        assertEquals("{}", str);
    }

    public static class Bean {
        public ClassLoader getMyClassLoader() {
            return this.getClass().getClassLoader();
        }
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        String str = JSON.toJSONString(bean);
        assertEquals("{}", str);
    }

    public static class Bean1 {
        public ClassLoader myClassLoader = this.getClass().getClassLoader();
    }
}
