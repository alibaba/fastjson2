package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GeneratedWriterReviewTest {
    @Test
    public void generatedNamesEscapeControlCharactersAndQuotes() {
        ObjectWriter<NamedBean> objectWriter = ObjectWriterCreatorASM.INSTANCE.createObjectWriter(NamedBean.class);
        assertTrue(objectWriter.getClass().getSimpleName().startsWith("OWG_"));
        for (JSONWriter writer : new JSONWriter[] {JSONWriter.ofUTF8(), JSONWriter.ofUTF16(),
                JSONWriter.of(JSONWriter.Feature.UseSingleQuotes)}) {
            try (JSONWriter output = writer) {
                objectWriter.write(output, new NamedBean());
                assertEquals(JSONObject.of("a\"b", 1, "c\nd", 2, "e'f", 3), JSON.parseObject(output.toString()));
            }
        }
    }

    @Test
    public void generatedListHandlesNullAndPolymorphicItems() {
        ListBean bean = new ListBean();
        bean.items = Arrays.asList(new Item(), null, new Item());
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray);
        ListBean result = JSONB.parseObject(bytes, ListBean.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals(3, result.items.size());
        assertNull(result.items.get(1));
        assertEquals(7, result.items.get(2).id);

        bean.items = Arrays.asList(new Item(), new SubItem());
        JSONObject object = (JSONObject) JSONB.parse(JSONB.toBytes(bean));
        assertEquals(9, object.getJSONArray("items").getJSONObject(1).getIntValue("extra"));
    }

    @Test
    public void arrayMappingRespectsNestedFieldFeature() {
        ObjectWriter<NestedBean> objectWriter = ObjectWriterCreatorASM.INSTANCE.createObjectWriter(NestedBean.class);
        try (JSONWriter writer = JSONWriter.ofJSONB()) {
            objectWriter.writeArrayMappingJSONB(writer, new NestedBean(), null, null, 0);
            JSONArray result = (JSONArray) JSONB.parse(writer.getBytes());
            assertInstanceOf(JSONObject.class, result.get(0));
            assertEquals(7, result.getJSONObject(0).getIntValue("id"));
            assertInstanceOf(JSONArray.class, result.get(1));
            assertEquals(7, result.getJSONArray(1).getIntValue(0));
        }
    }

    @Test
    public void generatedListRetainsCollectionType() {
        ObjectWriter<ListBean> objectWriter = ObjectWriterCreatorASM.INSTANCE.createObjectWriter(ListBean.class);
        assertTrue(objectWriter.getFieldWriter("items").getClass().getSimpleName().startsWith("OWF_"));
        ListBean bean = new ListBean();
        bean.items = new LinkedList<>(Arrays.asList(new Item(), null, new Item()));
        try (JSONWriter writer = JSONWriter.ofJSONB(JSONWriter.Feature.BeanToArray, JSONWriter.Feature.WriteClassName)) {
            objectWriter.writeJSONB(writer, bean, null, null, 0);
            ListBean result = JSONB.parseObject(writer.getBytes(), ListBean.class,
                    JSONReader.Feature.SupportArrayToBean, JSONReader.Feature.SupportAutoType);
            assertInstanceOf(LinkedList.class, result.items);
            assertEquals(3, result.items.size());
            assertNull(result.items.get(1));
        }
    }

    @Test
    public void contextArrayMappingAppliesToNestedBean() {
        ObjectWriter<NestedBean> objectWriter = ObjectWriterCreatorASM.INSTANCE.createObjectWriter(NestedBean.class);
        try (JSONWriter writer = JSONWriter.ofJSONB(JSONWriter.Feature.BeanToArray)) {
            objectWriter.writeArrayMappingJSONB(writer, new NestedBean(), null, null, 0);
            JSONArray result = (JSONArray) JSONB.parse(writer.getBytes());
            assertInstanceOf(JSONArray.class, result.get(0));
            assertInstanceOf(JSONArray.class, result.get(1));
            assertEquals(7, result.getJSONArray(0).getIntValue(0));
        }
    }

    public static class NamedBean {
        @JSONField(name = "a\"b")
        public int a = 1;
        @JSONField(name = "c\nd")
        public int b = 2;
        @JSONField(name = "e'f")
        public int c = 3;
    }

    public static class ListBean {
        public List<Item> items;
    }

    public static class Item {
        public int id = 7;
    }

    public static class SubItem extends Item {
        public int extra = 9;
    }

    public static class NestedBean {
        public Item a = new Item();
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public Item b = new Item();
    }
}
