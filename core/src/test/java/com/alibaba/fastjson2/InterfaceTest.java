package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InterfaceTest {
    @Test
    public void test() {
        if (TestUtils.GRAALVM || TestUtils.ANDROID) {
            return;
        }

        String str = "{\"item\":{\"id\":123}}";

        Bean bean = JSON.parseObject(str, Bean.class);
        assertNotNull(bean.item);
        assertEquals(123, bean.item.getId());

        JSONObject object = JSON.parseObject(str);
        Bean bean2 = object.toJavaObject(Bean.class);
        assertNotNull(bean2.item);
        assertEquals(123, bean2.item.getId());

        HashMap<Object, Object> map = new HashMap<>();
        map.put("id", 234);
        Bean bean3 = JSONObject.of("item", map).toJavaObject(Bean.class);
        assertNotNull(bean3.item);
        assertEquals(234, bean3.item.getId());
    }

    public static class Bean {
        public Item item;
    }

    public interface Item {
        int getId();

        void setId(int id);
    }
}
