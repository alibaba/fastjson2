package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.security.ProtectionDomain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2562 {
    @Test
    public void test() {
        Bean bean = new Bean();
        String str = JSON.toJSONString(bean);
        assertEquals("{}", str);
    }

    @Test
    public void test1() {
        String str = JSONObject.of("", new JSONObject()).toJSONString();
        Bean bean = JSON.parseObject(str, Bean.class);
        assertNotNull(bean);
    }

    public static class Bean {
        public ProtectionDomain getProtectionDomain() {
            throw new IllegalStateException();
        }

        public void setProtectionDomain(ProtectionDomain protectionDomain) {
            throw new IllegalStateException();
        }
    }
}
