package com.alibaba.fastjson2.adapter.jackson.databind.deser;

import com.alibaba.fastjson2.adapter.jackson.databind.DeserializationContext;
import com.alibaba.fastjson2.adapter.jackson.databind.ObjectMapper;
import com.alibaba.fastjson2.adapter.jackson.databind.deser.std.FromStringDeserializer;
import com.alibaba.fastjson2.adapter.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FromStringDeserializerTest {
    @Test
    public void test() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(StrValue.class, new FromStringDeserializer<StrValue>(StrValue.class) {
            @Override
            protected StrValue _deserialize(String value, DeserializationContext context) {
                return new StrValue(value);
            }
        });

        mapper.registerModule(module);

        StrValue strValue = mapper.readValue("\"abc\"", StrValue.class);
        assertEquals("abc", strValue.value);
    }

    public static class StrValue {
        private final String value;

        public StrValue(String value) {
            this.value = value;
        }
    }
}
