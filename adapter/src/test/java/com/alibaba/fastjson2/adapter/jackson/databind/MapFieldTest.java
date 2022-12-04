package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.adapter.jackson.core.JsonGenerator;
import com.alibaba.fastjson2.adapter.jackson.databind.annotation.JsonDeserialize;
import com.alibaba.fastjson2.adapter.jackson.databind.annotation.JsonSerialize;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapFieldTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.values = new HashMap<>();
        bean.values.put(new Key(123), 123);

        String str = JSON.toJSONString(bean);
        assertEquals("{\"values\":{\"123\":123}}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        Key key = bean1.values.keySet().stream().findFirst().get();
        assertEquals(123, key.value);
    }

    public static class Bean {
        @JsonSerialize(keyUsing = KeySerializer.class)
        @JsonDeserialize(keyUsing = KeyDeserializer.class)
        public Map<Key, Object> values;
    }

    public static class Key {
        public final int value;

        public Key(int value) {
            this.value = value;
        }
    }

    public static class KeyDeserializer
            extends com.alibaba.fastjson2.adapter.jackson.databind.KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
            int value = Integer.parseInt(key);
            return new Key(value);
        }
    }

    public static class KeySerializer
            extends JsonSerializer {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Key key = (Key) value;
            gen.writeString(Integer.toString(key.value));
        }
    }
}
