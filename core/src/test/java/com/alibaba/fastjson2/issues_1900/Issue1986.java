package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1986 {
    @Test
    public void test() {
        Role adminRole = new Role("admin", "管理员");
        Role userRole = new Role("user", "用户");
        List<Role> list = new ArrayList<>();
        list.add(adminRole);
        list.add(userRole);
        User user = new User("root", "123456", list);
        String json = JSON.toJSONString(user);
        User newUser = JSON.parseObject(json, User.class);
        assertEquals(json, JSON.toJSONString(newUser));
    }

    @Data
    public static class User {
        private final String username;
        private final String password;
        private final List<Role> roles;
    }

    @Data
    public static class Role {
        private final String name;
        private final String description;
    }
}
