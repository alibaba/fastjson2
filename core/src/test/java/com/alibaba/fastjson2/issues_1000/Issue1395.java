package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1395 {
    @Test
    public void test() {
        User user = new User();
        user.Name = "wenshao";
        assertEquals("{\"Name111\":\"wenshao\"}", JSON.toJSONString(user));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class User {
        @JSONField(name = "Name111")
        private String Name;
    }
}
