package com.alibaba.fastjson.issue_1600;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1662_1 {
    @Test
    public void test_for_issue() throws Exception {
        String json = "{\"value\":123}";
        Model model = JSON.parseObject(json, Model.class);
        assertEquals(12300, model.value);
        assertEquals("{\"value\":\"12300元\"}", JSON.toJSONString(model));
    }

    public static class Model {
        @JSONField(serializeUsing = ModelValueSerializer.class, deserializeUsing = ModelValueDeserializer.class)
        public int value;
    }

    public static class ModelValueSerializer
            implements ObjectSerializer {
        public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType,
                          int features) throws IOException {
            Integer value = (Integer) object;
            String text = value + "元";
            serializer.write(text);
        }
    }

    public static class ModelValueDeserializer
            implements ObjectDeserializer {
        public Integer deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            Object val = parser.parse();
            return ((Integer) val).intValue() * 100;
        }

        public int getFastMatchToken() {
            return 0;
        }
    }
}
