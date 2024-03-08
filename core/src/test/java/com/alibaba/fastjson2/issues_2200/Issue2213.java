package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReader;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue2213 {
    @Test
    public void test() {
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject("{\"values\":[1]}", TextEntity.class, JSONReader.Feature.FieldBased));
    }

    public static class ListIntReader
            implements ObjectReader<Object> {
        public static final ListIntReader INSTANCE = new ListIntReader();

        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            throw new JSONException("TODO");
        }
    }

    private class TextEntity {
        @JSONField(deserializeUsing = ListIntReader.class, serializeUsing = ListIntReader.class)
        public List<Integer> values;
    }
}
