package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.PropertyPreFilter;
import com.alibaba.fastjson.serializer.SerialContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2590 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.item0 = new Item(101);
        bean.item1 = new Item(102);
        bean.item2 = new Item(103);

        FilterDemo filter = new FilterDemo();
        String json = JSON.toJSONString(bean, filter);
        assertEquals("{\"item0\":{\"id\":101},\"item1\":{\"id\":102},\"item2\":{}}", json);
    }

    public static class FilterDemo
            implements PropertyPreFilter {
        @Override
        public boolean apply(JSONSerializer serializer, Object object, String name) {
            SerialContext context = serializer.getContext();
            if (context.toString().startsWith("$.item2")) {
                return false;
            }
            return true;
        }
    }

    public static class Bean {
        public Item item0;
        public Item item1;
        public Item item2;
    }

    public static class Item {
        public int id;

        public Item() {
        }

        public Item(int id) {
            this.id = id;
        }
    }
}
