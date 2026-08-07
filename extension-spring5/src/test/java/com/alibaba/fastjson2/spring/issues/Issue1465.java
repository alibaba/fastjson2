package com.alibaba.fastjson2.spring.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1465 {
    @Test
    public void test() {
        TestUser testUser = new TestUser();
        testUser.setUsername("test");
        testUser.setPassword("123456");
        testUser.setAuthorities(new HashSet<>());
        String s = JSON.toJSONString(testUser);
        assertEquals("{\"password\":\"123456\",\"username\":\"test\"}", s);
    }

    @Data
    public static class TestUser
            implements UserDetails {
        private String username;

        private String password;

        @JSONField(serialize = false)
        private Set<GrantedAuthority> authorities;

        @Override
        @JSONField(serialize = false)
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        @JSONField(serialize = false)
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        @JSONField(serialize = false)
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        @JSONField(serialize = false)
        public boolean isEnabled() {
            return true;
        }
    }
}
