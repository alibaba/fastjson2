package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ToJavaObjectTest {
    @Test
    public void test() {
        Root root = new Root();
        root.items = new HashMap<>();
        root.items.put("k", new HashMap<>());
        Item item = new Item();
        item.id = 1001;
        root.items.get("k").put("v", item);

        String str = JSON.toJSONString(root);
        assertEquals("{\"items\":{\"k\":{\"v\":{\"id\":1001}}}}", str);
        JSONObject jsonObject = JSON.parseObject(str);
        Root root1 = jsonObject.to(Root.class);
        assertEquals(1001, root1.items.get("k").get("v").id);
    }

    public static class Root {
        public Map<String, Map<String, Item>> items;
    }

    public static class Item {
        public int id;
    }
}
