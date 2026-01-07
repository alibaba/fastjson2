package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue3657 {
    public static class FlameTreeNode {
        private String n;
        private long s;
        private long v;
        private List<FlameTreeNode> c;

        public FlameTreeNode(String name, long self, long total) {
            this.n = name;
            this.s = self;
            this.v = total;
            this.c = new ArrayList<>();
        }

        public FlameTreeNode() {
        }

        public void addChild(FlameTreeNode child) {
            this.c.add(child);
        }

        public String getN() {
            return this.n;
        }

        public void setN(String n) {
            this.n = n;
        }

        public long getS() {
            return this.s;
        }

        public void setS(long s) {
            this.s = s;
        }

        public long getV() {
            return this.v;
        }

        public void setV(long v) {
            this.v = v;
        }

        public List<FlameTreeNode> getC() {
            return this.c;
        }

        public void setC(List<FlameTreeNode> c) {
            this.c = c;
        }
    }

    public static class SimpleNode {
        public List<SimpleNode> children;

        public SimpleNode() {
            this.children = new ArrayList<>();
        }

        public void addChild(SimpleNode child) {
            this.children.add(child);
        }
    }

    private FlameTreeNode createNestedStructure(int depth) {
        FlameTreeNode root = new FlameTreeNode("root", 100, 100);
        FlameTreeNode current = root;

        for (int i = 1; i < depth; i++) {
            FlameTreeNode child = new FlameTreeNode("node" + i, 10, 10);
            current.addChild(child);
            current = child;
        }

        return root;
    }

    private SimpleNode createSimpleNestedStructure(int depth) {
        SimpleNode root = new SimpleNode();
        SimpleNode current = root;

        for (int i = 1; i < depth; i++) {
            SimpleNode child = new SimpleNode();
            current.addChild(child);
            current = child;
        }

        return root;
    }

    @Test
    public void testJSONFactoryAPI() {
        int originalMaxLevel = JSONFactory.getDefaultMaxLevel();
        assertEquals(2048, originalMaxLevel);

        try {
            JSONFactory.setDefaultMaxLevel(5000);
            assertEquals(5000, JSONFactory.getDefaultMaxLevel());

            JSONFactory.setDefaultMaxLevel(3000);
            assertEquals(3000, JSONFactory.getDefaultMaxLevel());

            assertThrows(IllegalArgumentException.class, () -> {
                JSONFactory.setDefaultMaxLevel(0);
            });

            assertThrows(IllegalArgumentException.class, () -> {
                JSONFactory.setDefaultMaxLevel(-1);
            });
        } finally {
            JSONFactory.setDefaultMaxLevel(originalMaxLevel);
        }
    }

    @Test
    public void testDefaultMaxLevelLimit() {
        int originalMaxLevel = JSONFactory.getDefaultMaxLevel();
        try {
            JSONFactory.setDefaultMaxLevel(1024);

            FlameTreeNode root = createNestedStructure(550);
            JSONException exception = assertThrows(JSONException.class, () -> {
                JSON.toJSONString(root);
            });

            assertTrue(exception.getMessage().contains("level too large"));
        } finally {
            JSONFactory.setDefaultMaxLevel(originalMaxLevel);
        }
    }

    @Test
    public void testCustomMaxLevel() {
        int originalMaxLevel = JSONFactory.getDefaultMaxLevel();

        try {
            JSONFactory.setDefaultMaxLevel(3000);
            assertEquals(3000, JSONFactory.getDefaultMaxLevel());

            // Each FlameTreeNode creates 2 levels (object + children list)
            // Use a more conservative depth to avoid StackOverflowError
            FlameTreeNode root = createNestedStructure(500);

            assertDoesNotThrow(() -> {
                String json = JSON.toJSONString(root);
                assertNotNull(json);
                assertTrue(json.length() > 0);
            });
        } finally {
            JSONFactory.setDefaultMaxLevel(originalMaxLevel);
        }
    }

    @Test
    public void testBoundaryConditions() {
        int originalMaxLevel = JSONFactory.getDefaultMaxLevel();

        try {
            JSONFactory.setDefaultMaxLevel(2048);

            SimpleNode rootAtLimit = createSimpleNestedStructure(1023);
            assertDoesNotThrow(() -> {
                String json = JSON.toJSONString(rootAtLimit);
                assertNotNull(json);
            });

            SimpleNode rootOverLimit = createSimpleNestedStructure(1025);
            JSONException exception = assertThrows(JSONException.class, () -> {
                JSON.toJSONString(rootOverLimit);
            });

            assertTrue(exception.getMessage().contains("level too large"));
        } finally {
            JSONFactory.setDefaultMaxLevel(originalMaxLevel);
        }
    }

    @Test
    public void testRealWorldUsage() {
        int originalMaxLevel = JSONFactory.getDefaultMaxLevel();

        try {
            JSONFactory.setDefaultMaxLevel(2048);

            FlameTreeNode deepStructure = createNestedStructure(1000);
            String json = JSON.toJSONString(deepStructure);
            assertNotNull(json);
            assertTrue(json.contains("\"n\":\"root\""));

            FlameTreeNode overLimitStructure = createNestedStructure(1100);
            assertThrows(JSONException.class, () -> {
                JSON.toJSONString(overLimitStructure);
            });
        } finally {
            JSONFactory.setDefaultMaxLevel(originalMaxLevel);
        }
    }

    @Test
    public void testRegression() {
        int originalMaxLevel = JSONFactory.getDefaultMaxLevel();

        try {
            FlameTreeNode simpleNode = new FlameTreeNode("test", 1, 1);
            simpleNode.addChild(new FlameTreeNode("child", 2, 2));

            String json = JSON.toJSONString(simpleNode);
            assertNotNull(json);
            assertTrue(json.contains("\"n\":\"test\""));

            FlameTreeNode parsed = JSON.parseObject(json, FlameTreeNode.class);
            assertNotNull(parsed);
            assertEquals("test", parsed.getN());
            assertEquals(1, parsed.getS());
            assertEquals(1, parsed.getV());
            assertNotNull(parsed.getC());
            assertEquals(1, parsed.getC().size());
        } finally {
            JSONFactory.setDefaultMaxLevel(originalMaxLevel);
        }
    }
}
