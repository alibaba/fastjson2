package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1761 {
    @Test
    public void test() {
        String json = "[{'userId':'123'}]";
        JSONArray jsonArray = JSON.parseArray(json);
        MyListImpl<User> myList = jsonArray.to(new TypeReference<MyListImpl<User>>() {}.getType());
        assertEquals(1, myList.size());
        assertEquals(123, myList.get(0).userId);

        MyListImpl<User> users = JSON.parseObject(json, new TypeReference<MyListImpl<User>>() {});
        assertEquals(1, users.size());
        assertEquals(123, users.get(0).userId);
    }

    public static class MyListImpl<E>
            extends ArrayList<E> {
    }

    public static class User {
        public int userId;
    }
}
