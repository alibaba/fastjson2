package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JSONBTableTest8 {
    @Test
    public void test_0() {
        ItemEvent event = new ItemEvent();

        Bean bean = new Bean();
        bean.values = new ArrayList<>();
        bean.values.add(new Item(event));
        bean.values.add(new Item(event));

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);
        System.out.println(JSONB.toJSONString(bytes));

        Bean bean2 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);

        assertEquals(bean.values.size(), bean2.values.size());
        assertNotNull(bean2.values.get(0).event);
        assertNotNull(bean2.values.get(1).event);
        assertSame(bean2.values.get(0).event, bean2.values.get(1).event);
    }

    @Test
    public void test_1() {
        ItemEvent event = new ItemEvent();

        Bean bean = new Bean();
        bean.values = new ArrayList<>();
        bean.values.add(new Item(event));
        bean.values.add(bean.values.get(0));

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean bean2 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);

        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertNotNull(bean2.values.get(0).event);
        assertNotNull(bean2.values.get(1).event);
        assertSame(bean2.values.get(0), bean2.values.get(1));
    }

    public static class Bean {
        List<Item> values;
    }

    public static class Item {
        public ItemEvent event;

        public Item(ItemEvent event) {
            this.event = event;
        }
    }

    public static class ItemEvent {
    }
}
