package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.reader.ObjectReader;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
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

    public static class Bean {
        public Entity entity;
    }

    public static class RawBean {
        public RawEntity entity;
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
}
