package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3649 {
    @Test
    public void test() {
        UserDTO user = new UserDTO();
        user.setUserName("张三");
        assertEquals("{\"UserName\":\"张三\"}",
                JSON.toJSONString(user));
    }

    @Data
    public static class UserDTO {
        private String userName;

        @JSONField(name = "UserName")
        public String getTheUserName() {
            return userName;
        }
    }
}
