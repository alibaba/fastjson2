package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
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

    @Test
    public void test_reflect() {
        UserDTO user = new UserDTO();
        user.setUserName("张三");
        JSONObject jsonObject = JSON.parseObject(
                ObjectWriterCreator.INSTANCE.createObjectWriter(UserDTO.class)
                        .toJSONString(user));
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

    @Test
    public void test1() {
        UserDTO1 user = new UserDTO1();
        user.setUserName("张三");
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(user));
        assertEquals(2, jsonObject.size());
        assertEquals(user.getUserName(), jsonObject.getString("UserName"));
        assertEquals(user.getUserName(), jsonObject.getString("userName"));
    }

    @Test
    public void test1_reflect() {
        UserDTO1 user = new UserDTO1();
        user.setUserName("张三");
        JSONObject jsonObject = JSON.parseObject(
                ObjectWriterCreator.INSTANCE.createObjectWriter(UserDTO1.class)
                        .toJSONString(user));
        assertEquals(2, jsonObject.size());
        assertEquals(user.getUserName(), jsonObject.getString("UserName"));
        assertEquals(user.getUserName(), jsonObject.getString("userName"));
    }

    @Data
    public static class UserDTO1 {
        private String userName;

        @JSONField(name = "UserName")
        public String getTheUserName() {
            return userName;
        }
        private String field1;
        private String field2;
        private String field3;
        private String field4;
        private String field5;
        private String field6;
        private String field7;
        private String field8;
        private String field9;
        private String field10;
        private String field11;
        private String field12;
        private String field13;
        private String field14;
        private String field15;
        private String field16;
        private String field17;
        private String field18;
        private String field19;
        private String field20;
        private String field21;
        private String field22;
        private String field23;
        private String field24;
        private String field25;
        private String field26;
        private String field27;
        private String field28;
        private String field29;
        private String field30;
        private String field31;
        private String field32;
        private String field33;
        private String field34;
        private String field35;
        private String field36;
        private String field37;
        private String field38;
        private String field39;
        private String field40;
        private String field41;
        private String field42;
        private String field43;
        private String field44;
        private String field45;
        private String field46;
        private String field47;
        private String field48;
        private String field49;
        private String field50;
        private String field51;
        private String field52;
        private String field53;
        private String field54;
        private String field55;
        private String field56;
        private String field57;
        private String field58;
        private String field59;
        private String field60;
        private String field61;
        //第62个序列化是2个
        private String field62;
        //第63个序列化是1个
        private String field63;
    }
}
