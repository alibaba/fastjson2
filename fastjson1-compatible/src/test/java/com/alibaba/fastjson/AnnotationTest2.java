package com.alibaba.fastjson;

import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnnotationTest2 {
    @Test
    public void test_codec() throws Exception {
        User user = new User();
        user.setId(1001);
        user.setName("bob.panl");
        user.setDescrition("大黄牛");

        String text = JSON.toJSONString(user);
        System.out.println(text);

        User user1 = JSON.parseObject(text, User.class);

        assertEquals(user1.getId(), user.getId());
        assertEquals(user1.getName(), user.getName());
    }

    public static class User {
        @JSONField(name = "ID")
        private int id;
        private String name;
        private String descrition;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JSONField(name = "desc")
        public String getDescrition() {
            return descrition;
        }

        @JSONField(name = "desc")
        public void setDescrition(String descrition) {
            this.descrition = descrition;
        }
    }
}
