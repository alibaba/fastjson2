package com.alibaba.fastjson2.v1issues.issue_1200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.NameFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by kimmking on 15/06/2017.
 */
public class Issue1274 {
    @Test
    public void test_for_issue() throws Exception {
        User user = new User();
        user.setId(1);
        user.setName("name");

        NameFilter filter = new NameFilter() {
            public String process(Object object, String name, Object value) {
                System.out.println("name=" + name + ",value=" + value);
                if (name.equals("name")) {
                    return "nt";
                }
                return name;
            }
        };

        // test for  JSON.toJSONString(user,filter);
        String jsonString = JSON.toJSONString(user, filter);
        System.out.println(jsonString);
        assertEquals("{\"id\":1,\"nt\":\"name\"}", jsonString);
    }

    public static class User {
        private int id;
        private String name;

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
    }
}
