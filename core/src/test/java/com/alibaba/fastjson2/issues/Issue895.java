package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author kraity
 */
public class Issue895 {
    @Test
    public void test22() {
        String text = "{\"a\":1,\"b\":\"asd\",\"c\":{\"a\":9},\"d\":{\"a\":10}}";
        User<DD> user = JSON.parseObject(
                text, new TypeReference<User<DD>>() {
                }
        );
        assertEquals(text, JSON.toJSONString(user));
        assertEquals(DD.class, user.c.getClass());
        assertEquals(DD.class, user.d.getClass());
    }

    static class DD {
        public int a;
    }

    static class User<T> {
        public int a;
        public String b;
        private T c;
        public T d;

        public T getC() {
            return c;
        }

        public void setC(T c) {
            this.c = c;
        }
    }
}
