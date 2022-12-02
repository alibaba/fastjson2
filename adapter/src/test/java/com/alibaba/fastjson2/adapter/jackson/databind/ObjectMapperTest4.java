package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.adapter.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectMapperTest4 {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void test() throws Exception {
        ObjectNode node = new ObjectNode();
        node.put("id", 123);
        node.put("name", "DataWorks");
        Bean bean = mapper.treeToValue(node, Bean.class);
        assertEquals(bean.id, node.get("id").asInt());
        assertEquals(bean.name, node.get("name").asText());
    }

    static class Bean {
        public int id;
        public String name;
    }
}
