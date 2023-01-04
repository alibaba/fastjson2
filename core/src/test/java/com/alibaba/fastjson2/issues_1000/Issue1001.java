package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1001 {
    @Test
    public void TestFastjson2() {
        User user = new User();
        user.setName("hello");
        Map<String, Object> map = new HashMap<>();
        map.put("score", Arrays.asList(111, 222, 333));
        map.put("classes", Arrays.asList("语文", "数学", "英语"));
        user.setValues(map);

        String values = JSONObject.toJSONString(user);
        JSONObject jsonObject = JSONObject.parseObject(values);
        User temp = jsonObject.to(User.class);
        assertEquals(user.name, temp.name);
    }

    public class User {
        private String name;
        private Map<String, Object> values;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, Object> getValues() {
            return values;
        }

        public void setValues(Map<String, Object> values) {
            this.values = values;
        }
    }
}
