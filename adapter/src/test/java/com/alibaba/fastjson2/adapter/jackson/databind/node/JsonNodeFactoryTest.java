package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.adapter.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class JsonNodeFactoryTest {
    @Test
    public void test() {
        ObjectMapper mapper = new ObjectMapper();
        assertNull(mapper.getNodeFactory().nullNode().asText());
    }
}
