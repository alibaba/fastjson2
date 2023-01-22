package com.alibaba.fastjson2.example.graalvm_native;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class Hello {
    @Test
    public void test() throws Exception {
        String jmvName = System.getProperty("java.vm.name");
        System.out.println("java.vm.name : " + jmvName);

        User user = new User(1, "雷卷");
        List list = new ArrayList();
        list.add(user);
        String jsonText = JSON.toJSONString(list);
        List<User> user2 = JSON.parseArray(jsonText, User.class);
        System.out.println(jsonText);
        System.out.println("Fastjson: " + user2.get(0).getNick());

        System.out.println(JSONReader.of("{}").getClass().getName());
        System.out.println(JSONReader.of("{}").getObjectReader(User.class).getClass());
    }

    public static class User {
        private Integer id;
        private String nick;

        public User() {
        }

        public User(Integer id, String nick) {
            this.id = id;
            this.nick = nick;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getNick() {
            return nick;
        }

        public void setNick(String nick) {
            this.nick = nick;
        }
    }
}
