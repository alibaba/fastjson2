package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JSONFromObjectTest {
    @Test
    public void test_0() throws Exception {
        User user = new User();
        user.setId(3);
        user.setName("周访");

        JSONObject json = (JSONObject) JSON.toJSON(user);

        assertEquals(new Long(3), json.getLong("id"));
        assertEquals("周访", json.getString("name"));
    }

    @Test
    public void test_1() throws Exception {
        JSONObject user = new JSONObject();
        user.put("id", 3);
        user.put("name", "周访");

        JSONObject json = (JSONObject) JSON.toJSON(user);

        assertEquals(new Long(3), json.getLong("id"));
        assertEquals("周访", json.getString("name"));
    }

    @Test
    public void test_2() throws Exception {
        HashMap user = new HashMap();
        user.put("id", 3);
        user.put("name", "周访");

        JSONObject json = (JSONObject) JSON.toJSON(user);

        assertEquals(new Long(3), json.getLong("id"));
        assertEquals("周访", json.getString("name"));
    }

    @Test
    public void test_3() throws Exception {
        List users = new ArrayList();
        HashMap user = new HashMap();
        user.put("id", 3);
        user.put("name", "周访");
        users.add(user);

        JSONArray array = (JSONArray) JSON.toJSON(users);
        JSONObject json = array.getJSONObject(0);

        assertEquals(new Long(3), json.getLong("id"));
        assertEquals("周访", json.getString("name"));
    }

    @Test
    public void test_error() throws Exception {
        C c = new C();

        JSONException error = null;
        try {
            JSON.toJSON(c);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
    }

    public static class User {
        private long id;
        private String name;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class C {
        public int getId() {
            throw new UnsupportedOperationException();
        }

        public void setId(int id) {
            throw new UnsupportedOperationException();
        }
    }
}
