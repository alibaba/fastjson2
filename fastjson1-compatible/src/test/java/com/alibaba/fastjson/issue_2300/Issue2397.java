package com.alibaba.fastjson.issue_2300;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2397 {
    @Test
    public void test_for_bug() {
        String jsonStr = "{\"items\":[{\"id\":1,\"name\":\"kata\"}]}";
        TestReply testReply = JSON.parseObject(jsonStr, new TypeReference<TestReply>() {
        });

        assertEquals(testReply.getItems().get(0).getId(), 1);
    }

    public static class SuperBaseReply<T> {
        private List<T> items;

        public List<T> getItems() {
            return items;
        }

        public void setItems(List<T> items) {
            this.items = items;
        }
    }

    public static class BaseReply<T>
            extends SuperBaseReply<T> {
    }

    public static class Msg
            implements Serializable {
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

        public Msg(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class TestReply
            extends BaseReply<Msg> {
    }
}
