package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2649 {
    @Test
    public void test() {
        Node node = new Node();
        node.strValue = "hello world";
        String s = JSON.toJSONString(node);
        assertEquals("{\"strValue\":\"hello world\"}", s);

        System.out.println(s);
        Node node1 = JSON.to(Node.class, s);
        assertEquals(node.strValue, node1.strValue);
    }

    @Data
    public static class Node {
        @JSONField(name = "strValue")
        @JsonProperty("str_value")
        private String strValue;
    }
}
