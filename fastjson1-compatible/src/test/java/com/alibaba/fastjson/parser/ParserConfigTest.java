package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ParserConfigTest {
    @Test
    public void test() {
        ParserConfig config = ParserConfig.global;
        assertEquals(ObjectReaderProvider.SAFE_MODE, config.isSafeMode());
        assertFalse(config.isAutoTypeSupport());
        config.checkAutoType(JSONObject.class);
        config.setAsmEnable(true);
        assertTrue(config.isAsmEnable());

        config.configFromPropety(new Properties());
    }

    @Test
    public void test1() {
        ParserConfig config = new ParserConfig();
        try {
            config.setSafeMode(true);
        } catch (Exception ignored) {
            // ignored
        }

        ParserConfig.AutoTypeCheckHandler handler = new ParserConfig.AutoTypeCheckHandler() {
            @Override
            public Class<?> handler(String typeName, Class<?> expectClass, int features) {
                return null;
            }
        };
        config.addAutoTypeCheckHandler(handler);
        config.addDeny("aaa");
        config.addDenyInternal("aaa");
        config.addAccept("aaa");

        Properties properties = new Properties();
        properties.put(ParserConfig.AUTOTYPE_ACCEPT, "a,b,c");
        properties.put(ParserConfig.DENY_PROPERTY, "e,f,g");

        ObjectDeserializer deserializer = config.get(Bean.class);
        assertNotNull(deserializer);

        deserializer = config.getDeserializer(Bean.class);
        assertNotNull(deserializer);

        deserializer = config.getDeserializer(Bean.class, Bean.class);
        assertNotNull(deserializer);

        Bean bean = deserializer.deserialze(new DefaultJSONParser("{\"id\":123}"), null, null);
        assertEquals(123, bean.id);
    }

    static class Bean {
        public int id;
    }

    public void test2() {
        ParserConfig config0 = new ParserConfig();
        ParserConfig config1 = new ParserConfig();
        assertNotSame(config0.provider, config1.provider);

        assertSame(JSONFactory.getDefaultObjectReaderProvider(), ParserConfig.getGlobalInstance().provider);
    }

    @Test
    public void propertyNamingStrategyField() {
        // Regression for issue #7704: fastjson 1.x ParserConfig exposes a public
        // propertyNamingStrategy field; migration code assigns it directly. The compat
        // package previously lacked the field, raising NoSuchFieldError. Mirror what
        // SerializeConfig already exposes.
        ParserConfig config = new ParserConfig();
        assertNull(config.propertyNamingStrategy);
        config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        assertSame(PropertyNamingStrategy.SnakeCase, config.propertyNamingStrategy);
    }
}
