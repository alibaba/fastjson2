package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2644 {
    final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testJsonBackReference() throws Exception {
        final JsonManagedReferenceDTO dto = new JsonManagedReferenceDTO();

        final Item item = new Item();
        item.setItemName("Item1");
        item.setUser(dto);

        dto.getItems().add(item);

        final String jsonJackson = mapper.writeValueAsString(dto);
        final String jsonFastjson2 = JSON.toJSONString(dto, JSONWriter.Feature.WriteNulls);

        assertEquals(jsonJackson, jsonFastjson2);
    }

    static class JsonManagedReferenceDTO {
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

    static class Item {
        private String itemName;

        @JsonBackReference
        private JsonManagedReferenceDTO user;

        public String getItemName() {
            return this.itemName;
        }

        public void setItemName(final String itemName) {
            this.itemName = itemName;
        }

        public JsonManagedReferenceDTO getUser() {
            return this.user;
        }

        public void setUser(final JsonManagedReferenceDTO user) {
            this.user = user;
        }
    }
}
