package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Canal_Issue4186 {
    final JSONReader.Filter autoTypeFilter = JSONReader.autoTypeFilter(Canal_Issue4186.class.getName());

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
    public void test1() {
        Position position = new Position() {

        };

        String jsonString = JSON.toJSONString(position, JSONWriter.Feature.WriteClassName);
        System.out.println(jsonString);
        Position position1 = JSON.parseObject(jsonString, Position.class, JSONReader.Feature.SupportAutoType);
        assertEquals(position.getClass(), position1.getClass());
    }

    public static abstract class Position
            implements Serializable {

    }

    public static class EntryPosition extends Position {

    }
}
