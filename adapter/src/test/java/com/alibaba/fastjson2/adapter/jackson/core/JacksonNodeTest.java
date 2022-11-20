package com.alibaba.fastjson2.adapter.jackson.core;

import com.alibaba.fastjson2.adapter.jackson.databind.JsonNode;
import com.alibaba.fastjson2.adapter.jackson.databind.ObjectMapper;
import com.alibaba.fastjson2.adapter.jackson.databind.node.ArrayNode;
import com.alibaba.fastjson2.adapter.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JacksonNodeTest {
    @Test
    public void testJsonNode() throws Exception {
        // 构建JSON树
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("id", "zhuan2quan");
        root.put("name", "程序新视界");
        ArrayNode interest = root.putArray("interest");
        interest.add("Java");
        interest.add("Spring Boot");
        interest.add("JVM");

        // JSON树转JSON字符串
        String json = mapper.writeValueAsString(root);
        assertEquals("{\"id\":\"zhuan2quan\",\"name\":\"程序新视界\",\"interest\":[\"Java\",\"Spring Boot\",\"JVM\"]}", json);
    }

    @Test
    public void testJsonToJsonNode() {
        String json = "{\"id\":\"zhuan2quan\",\"name\":\"程序新视界\",\"interest\":[\"Java\",\"Spring Boot\",\"JVM\"]}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);
        JsonNode nameNode = jsonNode.path("name");
        assertFalse(nameNode.isArray());
        String name = nameNode.asText();
        System.out.println(name);

        JsonNode interestNode = jsonNode.get("interest");
        assertTrue(interestNode.isArray());
        for (JsonNode node : interestNode) {
            System.out.println(node.asText());
        }
    }
}
