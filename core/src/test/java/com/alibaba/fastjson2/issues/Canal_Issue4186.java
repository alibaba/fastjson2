package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.filter.Filter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Canal_Issue4186 {
    final Filter autoTypeFilter = JSONReader.autoTypeFilter(Canal_Issue4186.class.getName());

    @Test
    public void test() {
        EntryPosition position = new EntryPosition();

        String jsonString = JSON.toJSONString(position, JSONWriter.Feature.WriteClassName);

        {
            Position position1 = JSON.parseObject(jsonString, Position.class, autoTypeFilter);
            assertEquals(position.getClass(), position1.getClass());
        }
        {
            Position position1 = JSON.parseObject(jsonString, Position.class, autoTypeFilter);
            assertEquals(position.getClass(), position1.getClass());
        }

        assertThrows(JSONException.class, () -> JSON.parseObject(jsonString, Position.class));
    }

    @Test
    public void testJSONB() {
        EntryPosition position = new EntryPosition();

        byte[] bytes = JSONB.toBytes(position, JSONWriter.Feature.WriteClassName);

        {
            Position position1 = JSONB.parseObject(bytes, Position.class, autoTypeFilter);
            assertEquals(position.getClass(), position1.getClass());
        }
        {
            Position position1 = JSONB.parseObject(bytes, Position.class, autoTypeFilter);
            assertEquals(position.getClass(), position1.getClass());
        }

        assertThrows(JSONException.class, () -> JSONB.parseObject(bytes, Position.class));
    }

    @Test
    public void test1() {
        Position position = new Position() {
        };

        String jsonString = JSON.toJSONString(position, JSONWriter.Feature.WriteClassName);
        System.out.println(jsonString);
        Position position1 = JSON.parseObject(jsonString, Position.class, JSONReader.Feature.SupportAutoType);
        assertEquals(position.getClass(), position1.getClass());
    }

    public abstract static class Position
            implements Serializable {
    }

    public static class EntryPosition
            extends Position {
    }
}
