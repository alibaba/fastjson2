package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArmoryTest {
    @Test
    public void test_item() throws Exception {
        Item item = new Item();
        String text = JSON.toJSONString(item, SerializerFeature.SortField, SerializerFeature.UseSingleQuotes);
        assertEquals("{'id':0,'name':'xx'}", text);
    }

    @Test
    public void test_0() throws Exception {
        List<Object> message = new ArrayList<Object>();
        MessageBody body = new MessageBody();
        Item item = new Item();
        body.getItems().add(item);

        message.add(new MessageHead());
        message.add(body);

        String text = JSON.toJSONString(message, SerializerFeature.SortField, SerializerFeature.UseSingleQuotes);
        assertEquals("[{},{'items':[{'id':0,'name':'xx'}]}]", text);
    }

    public static class Item {
        private int id;
        private String name = "xx";

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class MessageHead {
    }

    public static class MessageBody {
        private List<Object> items = new ArrayList<Object>();

        public List<Object> getItems() {
            return items;
        }

        public void setItems(List<Object> items) {
            this.items = items;
        }
    }
}
