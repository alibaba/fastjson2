package com.alibaba.fastjson2.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonManagedReferenceTest {
    final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testJsonBackReference() throws Exception {
        final Bean dto = new Bean();

        final Item item = new Item();
        item.setItemName("Item1");
        item.setUser(dto);

        dto.getItems().add(item);

        final String jsonJackson = mapper.writeValueAsString(dto);
        final String jsonFastjson2 = JSON.toJSONString(dto, JSONWriter.Feature.WriteNulls);

        Map<String, Object> mapJackson = mapper.readValue(jsonJackson, Map.class);
        Map<String, Object> mapFastjson2 = mapper.readValue(jsonFastjson2, Map.class);
        assertEquals(mapJackson, mapFastjson2);
    }

    public static class Bean {
        String id;

        @JsonManagedReference
        private List<Item> items = new ArrayList<>();

        public String getId() {
            return this.id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public List<Item> getItems() {
            return this.items;
        }

        public void setItems(final List<Item> items) {
            this.items = items;
        }
    }

    public static class Item {
        private String itemName;

        @JsonBackReference
        private Bean user;

        public String getItemName() {
            return this.itemName;
        }

        public void setItemName(final String itemName) {
            this.itemName = itemName;
        }

        public Bean getUser() {
            return this.user;
        }

        public void setUser(final Bean user) {
            this.user = user;
        }
    }

    @Test
    public void test1() throws Exception {
        final Bean1 dto = new Bean1();

        final Item1 item = new Item1();
        item.setItemName("Item1");
        item.setUser(dto);

        dto.getItems().add(item);

        final String jsonJackson = mapper.writeValueAsString(dto);
        final String jsonFastjson2 = JSON.toJSONString(dto, JSONWriter.Feature.WriteNulls);

        Map<String, Object> mapJackson = mapper.readValue(jsonJackson, Map.class);
        Map<String, Object> mapFastjson2 = mapper.readValue(jsonFastjson2, Map.class);
        assertEquals(mapJackson, mapFastjson2);
    }

    static class Bean1 {
        String id;

        @JsonManagedReference
        private List<Item1> items = new ArrayList<>();

        public String getId() {
            return this.id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public List<Item1> getItems() {
            return this.items;
        }

        public void setItems(final List<Item1> items) {
            this.items = items;
        }
    }

    static class Item1 {
        private String itemName;

        @JsonBackReference
        private Bean1 user;

        public String getItemName() {
            return this.itemName;
        }

        public void setItemName(final String itemName) {
            this.itemName = itemName;
        }

        public Bean1 getUser() {
            return this.user;
        }

        public void setUser(final Bean1 user) {
            this.user = user;
        }
    }
}
