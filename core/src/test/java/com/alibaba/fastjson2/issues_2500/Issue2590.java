package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerialContext;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.PropertyPreFilter;
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
        String json = JSON.toJSONString(bean, filter, JSONWriter.Feature.ReferenceDetection);
        assertEquals("{\"item0\":{\"id\":101},\"item1\":{\"id\":102},\"item2\":{}}", json);
    }

    public static class FilterDemo
            implements PropertyPreFilter {
        @Override
        public boolean process(JSONWriter writer, Object source, String name) {
            String parentPath = writer.getPath();
            return !parentPath.startsWith("$.item2");
        }
    }

    @Test
    public void test1() {
        Bean bean = new Bean();
        bean.item0 = new Item(101);
        bean.item1 = new Item(102);
        bean.item2 = new Item(103);

        FilterDemo1 filter = new FilterDemo1();
        String json = com.alibaba.fastjson.JSON.toJSONString(bean, filter);
        assertEquals("{\"item0\":{\"id\":101},\"item1\":{\"id\":102},\"item2\":{}}", json);
    }

    public static class FilterDemo1
            implements com.alibaba.fastjson.serializer.PropertyPreFilter {
        @Override
        public boolean apply(JSONSerializer serializer, Object object, String name) {
            SerialContext context = serializer.getContext();
            return !context.toString().startsWith("$.item2");
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
