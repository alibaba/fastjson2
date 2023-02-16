package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue1106 {
    @Test
    public void test() {
        assertSame(UserStatus.NORMAL, JSON.parseObject("{\"state\":0}").toJavaObject(User.class).state);
        assertSame(UserStatus.LOCKED, JSON.parseObject("{\"state\":1}").toJavaObject(User.class).state);
        assertNull(JSON.parseObject("{\"state\":-1}").toJavaObject(User.class).state);
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
