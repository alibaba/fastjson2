package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ArrayRefTest {
    @Test
    public void test_0() throws Exception {
        String text;
        {
            List<Group> groups = new ArrayList<Group>();

            Group g0 = new Group(0);
            Group g1 = new Group(1);
            Group g2 = new Group(2);

            groups.add(g0);
            groups.add(g1);
            groups.add(g2);
            groups.add(g0);
            groups.add(g1);
            groups.add(g2);

            text = JSON.toJSONString(groups);
        }

        System.out.println(text);
        assertEquals("[{\"id\":0},{\"id\":1},{\"id\":2},{\"$ref\":\"$[0]\"},{\"$ref\":\"$[1]\"},{\"$ref\":\"$[2]\"}]", text);

        List<Group> groups = JSON.parseObject(text, new TypeReference<List<Group>>() {});
        assertEquals(6, groups.size());

        assertSame(groups.get(0), groups.get(3));
        assertSame(groups.get(1), groups.get(4));
        assertSame(groups.get(2), groups.get(5));

        assertEquals(0, groups.get(0).getId());
        assertEquals(1, groups.get(1).getId());
        assertEquals(2, groups.get(2).getId());
        assertEquals(0, groups.get(3).getId());
        assertEquals(1, groups.get(4).getId());
        assertEquals(2, groups.get(5).getId());
    }

    public static class Group {
        private int id;

        public Group() {
        }

        public Group(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String toString() {
            return "{id:" + id + "}";
        }
    }
}
