package com.alibaba.fastjson2.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;

public class ArrayNodeTest {
    @Test
    public void test() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = (ArrayNode) mapper.readTree("[1,2,3]");
        System.out.println(arrayNode);
        System.out.println(JSON.toJSONString(arrayNode));
    }
}
