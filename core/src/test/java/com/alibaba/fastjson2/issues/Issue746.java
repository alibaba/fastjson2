package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue746 {
    @Test
    public void test() {
        LoginUser loginUser = JSON.parseObject("{\"menuSet\":Set[],\"authorities\":Set[]}", LoginUser.class);
        assertEquals(0, loginUser.menuSet.size());
        assertEquals(0, loginUser.authorities.size());
    }

    @Data
    @NoArgsConstructor
    public static class LoginUser {
        private User user;
        private Set<MenuVo> menuSet;
        private Set<? extends GrantedAuthority> authorities;

        public LoginUser(User user) {
            this.user = user;
        }
    }

    public static class MenuVo {
    }

    public static class GrantedAuthority {
    }

    public static class User {
    }
}
