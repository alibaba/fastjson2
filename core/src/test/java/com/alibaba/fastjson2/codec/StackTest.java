package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Stack;

public class StackTest {
    @Test
    public void test() {
        Stack<Node> nodestack = new Stack<>();
        Node node = new Node();
        node.setName("bb");
        nodestack.push(node);

        String str = JSON.toJSONString(nodestack);

        Stack result = JSON.parseObject(str, new com.alibaba.fastjson2.TypeReference<Stack<Node>>() {
        });
        System.out.println(result);
    }

    @Data
    public static class Node
            extends HashMap<String, Object> {
        private String getName() {
            return this.get("name").toString();
        }

        public void setName(String name) {
            this.put("name", name);
        }
    }
}
