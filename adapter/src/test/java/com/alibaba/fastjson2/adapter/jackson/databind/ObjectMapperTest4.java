package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.adapter.jackson.databind.node.ArrayNode;
import com.alibaba.fastjson2.adapter.jackson.databind.node.JsonNodeType;
import com.alibaba.fastjson2.adapter.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void readTree() throws Exception {
        String str = "{\"id\":123}";
        assertEquals(
                123,
                mapper.readTree(new ByteArrayInputStream(str.getBytes()))
                        .get("id")
                        .asInt()
        );
        assertEquals(
                123,
                mapper.readTree(new StringReader(str))
                        .get("id")
                        .asInt()
        );
        assertEquals(
                123,
                mapper.readTree(str)
                        .get("id")
                        .asInt()
        );
        assertEquals(
                123,
                ((JsonNode) mapper.readTree(mapper.factory.createParser(str)))
                        .get("id")
                        .asInt()
        );
    }

    @Test
    public void test1() {
        ArrayNode arrayNode = mapper.createArrayNode();
        assertFalse(arrayNode.elements().hasNext());
        assertFalse(arrayNode.iterator().hasNext());
        assertNull(arrayNode.get(0));
        assertEquals(JsonNodeType.ARRAY, arrayNode.getNodeType());
    }

    static class Bean {
        public int id;
        public String name;
    }
}
