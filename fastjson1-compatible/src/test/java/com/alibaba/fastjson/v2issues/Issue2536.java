package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue2536 {
    @Test
    public void test() {
        assertSame(UserStatus.NORMAL, JSON.parseObject("{\"state\":0}").toJavaObject(User.class).state);
        assertEquals(UserStatus.LOCKED, JSON.parseObject("{\"state\":1}").toJavaObject(User.class).state);
        assertNull(JSON.parseObject("{\"state\":-1}").toJavaObject(User.class).state);
    }

    @Test
    public void testfj() {
        assertSame(UserStatus.NORMAL, com.alibaba.fastjson.JSON.parseObject("{\"state\":0}").toJavaObject(User.class).state);
        assertEquals(UserStatus.LOCKED, com.alibaba.fastjson.JSON.parseObject("{\"state\":1}").toJavaObject(User.class).state);
        assertNull(com.alibaba.fastjson.JSON.parseObject("{\"state\":-1}").toJavaObject(User.class).state);
    }

    public enum UserStatus {
        NORMAL(0),
        LOCKED(1);

        private final byte code;

        UserStatus(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return this.code;
        }
    }

    public class User {
        public UserStatus state;
    }
}
