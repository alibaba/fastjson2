package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class Issue1567 {
    @Test
    void toJavaList() {
        String data = "[{\"id\":\"1001\",\"name\":\"Jack\"},{\"id\":\"1002\",\"name\":\"Rose\"}]\n";
        JSONArray array = JSONArray.parse(data);
        List<User> userList = array.toJavaList(User.class);
        assertEquals(2, userList.size());
        Optional<User> first = userList.stream().filter(i -> "1001".equals(i.getId())).findFirst();
        assertTrue(first.isPresent());
        User user = first.get();
        assertEquals("1001", user.getId());
        // name 或者nickname会为null， 之前用的版本2.0.25是没问题的
        assertNull(user.getName());
        assertEquals("Jack", user.getNickname());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class User {
        @JSONField(name = "id")
        private String id;
        @JSONField(name = "name")
        private String name;
        @JSONField(name = "name")
        private String nickname;
    }
}
