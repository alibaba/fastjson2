package com.alibaba.fastjson2.android;

import static org.junit.Assert.assertEquals;

import com.alibaba.fastjson.JSON;

import org.junit.Test;

public class Compatible1xTest {
    @Test
    public void test_parseObject() {
        User user = JSON.parseObject(
                "{\"id\":1,\"name\":\"kraity\"}", User.class
        );

        assertEquals(1, user.id);
        assertEquals("kraity", user.name);

        String str = JSON.toJSONString(user);
        System.out.println(str);
    }

    static class User {
        public int id;
        public String name;
    }
}
