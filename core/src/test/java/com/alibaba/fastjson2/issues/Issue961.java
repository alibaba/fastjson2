package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue961 {
    @Test
    public void test() {
        User user1 = new User(1038L);
        String res1 = JSON.toJSONString(user1, JSONWriter.Feature.WriteLongAsString);
        assertEquals("{\"id\":\"1038\"}", res1);

        User user2 = new User(1039L);
        String res2 = JSON.toJSONString(user2, JSONWriter.Feature.WriteLongAsString);
        assertEquals("{\"id\":\"1039\"}", res2);
    }

    public class User {
        private long id;

        public User(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }
}
