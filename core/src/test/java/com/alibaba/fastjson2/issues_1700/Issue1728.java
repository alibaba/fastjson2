package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1728 {
    @Test
    public void test() {
        LoginUser user = new LoginUser();
        assertEquals("{}", JSON.toJSONString(user));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginUser
            implements UserDetails {
        @JSONField(serialize = false)
        private List<GrantedAuthority> authorities;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            throw new IllegalStateException();
        }
    }

    public interface UserDetails {
        Collection<? extends GrantedAuthority> getAuthorities();
    }

    public static class GrantedAuthority{
    }
}
