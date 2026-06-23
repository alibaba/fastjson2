package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.reader.ObjectReader;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue7636 {
    @Test
    public void testCustomReaderForMapSubtypeField() {
        Bean bean = JSON.parseObject("{\"entity\":{\"id\":123}}", Bean.class);

        assertEquals(123, bean.entity.id);
        assertEquals("custom", bean.entity.get("reader"));
    }

    @Test
    public void testCustomReaderForMapSubtype() {
        Entity entity = JSON.parseObject("{\"id\":123}", Entity.class);

        assertEquals(123, entity.id);
        assertEquals("custom", entity.get("reader"));
    }

    @Test
    public void testMapSubtypeWithoutCustomReader() {
        RawBean bean = JSON.parseObject("{\"entity\":{\"id\":123}}", RawBean.class);

        assertEquals(0, bean.entity.id);
        assertEquals(123, bean.entity.get("id"));
    }

    @Test
    public void testCustomReaderForMapSubtypeFieldJSONB() {
        byte[] bytes = JSONB.toBytes(JSON.parseObject("{\"entity\":{\"id\":123}}"));
        Bean bean = JSONB.parseObject(bytes, Bean.class);

        assertEquals(0, bean.entity.id);
        assertEquals(123, bean.entity.get("id"));
    }

    @Test
    public void testCustomReaderForListSubtypeField() {
        ListBean bean = JSON.parseObject("{\"items\":[\"a\",\"b\"]}", ListBean.class);

        assertEquals(2, bean.items.count);
        assertEquals("custom", bean.items.get(0));
    }

    @Test
    public void testCustomReaderForListSubtypeFieldJSONB() {
        byte[] bytes = JSONB.toBytes(JSON.parseObject("{\"items\":[\"a\",\"b\"]}"));
        ListBean bean = JSONB.parseObject(bytes, ListBean.class);

        assertEquals(2, bean.items.size());
        assertEquals("a", bean.items.get(0));
        assertEquals("b", bean.items.get(1));
    }

    @Test
    public void testReadOnlyCustomReaderForMapSubtypeFieldJSONB() {
        byte[] bytes = JSONB.toBytes(JSON.parseObject("{\"entity\":{\"id\":123}}"));
        ReadOnlyBean bean = JSONB.parseObject(bytes, ReadOnlyBean.class);

        assertEquals(0, bean.getEntity().id);
        assertEquals(123, bean.getEntity().get("id"));
    }

    @Test
    public void testReadOnlyCustomReaderForListSubtypeFieldJSONB() {
        byte[] bytes = JSONB.toBytes(JSON.parseObject("{\"items\":[\"a\",\"b\"]}"));
        ReadOnlyListBean bean = JSONB.parseObject(bytes, ReadOnlyListBean.class);

        assertEquals(2, bean.getItems().size());
        assertEquals("a", bean.getItems().get(0));
        assertEquals("b", bean.getItems().get(1));
    }

    @Test
    public void testCustomReaderForListSubtype() {
        EntityList list = JSON.parseObject("[\"a\",\"b\"]", EntityList.class);

        assertEquals(2, list.count);
        assertEquals("custom", list.get(0));
    }

    @Test
    public void testListSubtypeWithoutCustomReader() {
        RawListBean bean = JSON.parseObject("{\"items\":[\"a\",\"b\"]}", RawListBean.class);

        assertEquals(2, bean.items.size());
        assertEquals("a", bean.items.get(0));
    }

    public static class Bean {
        public Entity entity;
    }

    public static class RawBean {
        public RawEntity entity;
    }

    public static class ListBean {
        public EntityList items;
    }

    public static class RawListBean {
        public RawEntityList items;
    }

    public static class ReadOnlyBean {
        private final Entity entity = new Entity();

        public Entity getEntity() {
            return entity;
        }
    }

    public static class ReadOnlyListBean {
        private final EntityList items = new EntityList();

        public EntityList getItems() {
            return items;
        }
    }

    @JSONType(deserializer = EntityReader.class)
    public static class Entity
            extends HashMap<String, Object> {
        public int id;
    }

    public static class RawEntity
            extends HashMap<String, Object> {
        public int id;
    }

    @JSONType(deserializer = EntityListReader.class)
    public static class EntityList
            extends ArrayList<String> {
        public int count;
    }

    public static class RawEntityList
            extends ArrayList<String> {
    }

    public static class EntityReader
            implements ObjectReader<Entity> {
        @Override
        public Entity readObject(JSONReader jsonReader, Type fieldType,
                                 Object fieldName, long features) {
            Entity entity = new Entity();
            jsonReader.nextIfObjectStart();
            for (; ; ) {
                if (jsonReader.nextIfObjectEnd()) {
                    break;
                }
                String name = jsonReader.readFieldName();
                if ("id".equals(name)) {
                    entity.id = jsonReader.readInt32Value();
                } else {
                    jsonReader.skipValue();
                }
            }
            entity.put("reader", "custom");
            return entity;
        }
    }

    public static class EntityListReader
            implements ObjectReader<EntityList> {
        @Override
        public EntityList readObject(JSONReader jsonReader, Type fieldType,
                                     Object fieldName, long features) {
            EntityList list = new EntityList();
            jsonReader.nextIfArrayStart();
            for (; ; ) {
                if (jsonReader.nextIfArrayEnd()) {
                    break;
                }
                jsonReader.skipValue();
                list.count++;
                jsonReader.nextIfComma();
            }
            list.add("custom");
            return list;
        }
    }
}
