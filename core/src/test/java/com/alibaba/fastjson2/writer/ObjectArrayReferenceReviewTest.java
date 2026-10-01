package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ObjectArrayReferenceReviewTest {
    @Test
    public void arrayItemsAndFollowingFieldsUseCorrectReferencePaths() {
        Bean bean = new Bean();
        Item item = new Item();
        bean.items = new Item[] {item, item};
        bean.other = item;
        ObjectWriterProvider provider = new ObjectWriterProvider(ObjectWriterCreator.INSTANCE);
        try (JSONWriter writer = JSONWriter.of(JSONFactory.createWriteContext(provider, JSONWriter.Feature.ReferenceDetection))) {
            writer.setRootObject(bean);
            writer.writeAny(bean);
            JSONObject parsed = JSON.parseObject(writer.toString());
            JSONArray items = parsed.getJSONArray("items");
            assertSame(items.get(0), items.get(1));
            assertSame(items.get(0), parsed.get("other"));
        }
    }

    @Test
    public void emptyArrayRetainsBeanToArrayPosition() {
        Bean bean = new Bean();
        bean.items = new Item[0];
        ObjectWriterProvider provider = new ObjectWriterProvider(ObjectWriterCreator.INSTANCE);
        try (JSONWriter writer = JSONWriter.of(JSONFactory.createWriteContext(provider,
                JSONWriter.Feature.BeanToArray, JSONWriter.Feature.NotWriteEmptyArray))) {
            writer.writeAny(bean);
            assertEquals("[[],null]", writer.toString());
        }
    }

    public static class Bean {
        public Item[] items;
        public Item other;
    }

    public static class Item {
        public int value = 1;
    }
}
