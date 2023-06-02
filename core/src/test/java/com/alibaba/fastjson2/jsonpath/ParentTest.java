package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParentTest {
    @Test
    public void test() {
        JSONPath path = JSONPath.of("$.posts[?(@.id == 1)].id");

        assertFalse(path.endsWithFilter());

        JSONPath p0 = path.getParent();
        assertEquals("$.posts[?(@.id == 1)]", p0.toString());

        assertTrue(p0.endsWithFilter());

        JSONPath p1 = p0.getParent();
        assertEquals("$.posts", p1.toString());
        assertFalse(p1.endsWithFilter());

        JSONPath p2 = p1.getParent();
        assertEquals("$", p2.toString());
        assertFalse(p2.endsWithFilter());

        JSONPath p3 = p2.getParent();
        assertNull(p3);
    }

    @Test
    public void test1() {
        JSONPath path = JSONPath.of("$[0][1][2].name");
        JSONPath p1 = path.getParent();
        assertEquals("$[0][1][2]", p1.toString());

        JSONPath p2 = p1.getParent();
        assertEquals("$[0][1]", p2.toString());

        JSONPath p3 = p2.getParent();
        assertEquals("$[0]", p3.toString());

        JSONPath p4 = p3.getParent();
        assertEquals("$", p4.toString());

        JSONPath p5 = p4.getParent();
        assertNull(p5);
    }

    @Test
    public void test2() {
        JSONPath path = JSONPath.of("$.posts[?(@.id == 1)]");
        assertTrue(path.endsWithFilter());

        JSONPath parent = path.getParent();
        assertEquals("$.posts", parent.toString());
        assertFalse(parent.endsWithFilter());
    }
}
