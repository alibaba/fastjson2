package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ParserConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaBeanDeserializerTest {
    @Test
    public void test() {
        JavaBeanDeserializer deserializer = new JavaBeanDeserializer(ParserConfig.global, Bean.class, Bean.class);
        DefaultJSONParser parser = new DefaultJSONParser("{\"id\":123}");
        Bean bean = (Bean) deserializer.deserialze(parser, null, null);
        assertEquals(123, bean.id);
    }

    @Test
    public void test1() {
        JavaBeanDeserializer deserializer = new JavaBeanDeserializer(ParserConfig.global, Bean.class, null);
        DefaultJSONParser parser = new DefaultJSONParser("{\"id\":123}");
        Bean bean = (Bean) deserializer.deserialze(parser, null, null);
        assertEquals(123, bean.id);
    }

    public static class Bean {
        public int id;
    }
}
