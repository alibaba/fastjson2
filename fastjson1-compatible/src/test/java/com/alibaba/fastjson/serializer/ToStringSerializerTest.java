package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class ToStringSerializerTest {
    @Test
    public void test() {
        Bean bean = new Bean("101");

        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.put(Bean.class, ToStringSerializer.instance);
        String str = JSONObject.toJSONString(bean, serializeConfig);
        assertEquals("\"101\"", str);

        ParserConfig parserConfig = new ParserConfig();
        parserConfig.putDeserializer(
                Bean.class, new ReadStringDeserializer<>(Bean::new)
        );

        Bean bean1 = JSON.parseObject(str, Bean.class, parserConfig);
        assertEquals(101, bean1.id);
    }

    public static class Bean {
        private int id;

        public Bean(String str) {
            this.id = Integer.parseInt(str);
        }

        public String toString() {
            return Integer.toString(101);
        }
    }

    static class ReadStringDeserializer<T>
            implements ObjectDeserializer {
        private final Function<String, T> function;

        public ReadStringDeserializer(Function<String, T> function) {
            this.function = function;
        }

        @Override
        public T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            JSONReader reader = parser.getLexer().getReader();
            String str = reader.readString();

            return function.apply(str);
        }
    }
}
