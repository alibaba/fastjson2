package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3649 {
    @Test
    public void test() {
        UserDTO user = new UserDTO();
        user.setUserName("张三");
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(user));
        assertEquals(2, jsonObject.size());
        assertEquals(user.getUserName(), jsonObject.getString("UserName"));
        assertEquals(user.getUserName(), jsonObject.getString("userName"));
    }

    @Data
    public static class UserDTO {
        private String userName;

        @JSONField(name = "UserName")
        public String getTheUserName() {
            return userName;
        }

        public String getUserName() {
            return userName;
        }
    }
}
