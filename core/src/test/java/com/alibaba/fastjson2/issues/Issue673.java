package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author kraity
 */
public class Issue673 {

    @Test
    public void test() {
        String json = JSON.toJSONString(new Bean());
        assertEquals("{\"me\":true,\"me2\":true,\"ok\":true,\"ok2\":true}", json);
    }

    static class Bean {

        public boolean isMe() {
            return true;
        }

        public boolean getMe2() {
            return true;
        }

        public Boolean isOk() {
            return Boolean.TRUE;
        }

        public Boolean getOk2() {
            return Boolean.TRUE;
        }
    }
}
