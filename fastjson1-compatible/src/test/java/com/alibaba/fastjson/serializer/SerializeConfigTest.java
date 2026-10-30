package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class SerializeConfigTest {
    @Test
    public void test() {
        SerializeConfig config = new SerializeConfig();
        config.addFilter(Bean.class, new SimplePropertyPreFilter("id"));

        config.put(JSONArray.class, ListSerializer.instance);
    }

    @Test
    public void independentWriterClassLoaders() {
        SerializeConfig first = new SerializeConfig();
        SerializeConfig second = new SerializeConfig();
        ObjectWriterCreator defaultCreator = new ObjectWriterProvider().getCreator();
        ObjectWriterCreator firstCreator = first.getProvider().getCreator();
        ObjectWriterCreator secondCreator = second.getProvider().getCreator();
        assertEquals(defaultCreator.getClass(), firstCreator.getClass());
        assertEquals(defaultCreator.getClass(), secondCreator.getClass());

        ObjectWriter firstWriter = first.getProvider().getObjectWriter(Value.class);
        ObjectWriter secondWriter = second.getProvider().getObjectWriter(Value.class);
        assertSame(firstWriter, first.getProvider().getObjectWriter(Value.class));
        if (defaultCreator instanceof ObjectWriterCreatorASM) {
            assertNotSame(defaultCreator, firstCreator);
            assertNotSame(firstCreator, secondCreator);
            assertNotSame(firstWriter.getClass().getClassLoader(), secondWriter.getClass().getClassLoader());
        } else {
            assertSame(defaultCreator, firstCreator);
        }
        assertEquals("{\"id\":7}", JSON.toJSONString(new Value(), first));
        assertEquals("{\"id\":7}", JSON.toJSONString(new Value(), second));
    }

    @Test
    public void isolatedRegistrations() {
        SerializeConfig first = new SerializeConfig();
        SerializeConfig second = new SerializeConfig();
        first.put(Value.class, (serializer, object, fieldName, fieldType, features) -> serializer.write("first"));
        second.put(Value.class, (serializer, object, fieldName, fieldType, features) -> serializer.write("second"));
        assertEquals("\"first\"", JSON.toJSONString(new Value(), first));
        assertEquals("\"second\"", JSON.toJSONString(new Value(), second));
        assertEquals("{\"id\":7}", JSON.toJSONString(new Value(), new SerializeConfig()));
    }

    @Test
    public void explicitProviderAndContextCreator() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        assertSame(provider, new SerializeConfig(provider).getProvider());
        assertSame(SerializeConfig.DEFAULT_PROVIDER, SerializeConfig.globalInstance.getProvider());
        assertSame(SerializeConfig.DEFAULT_PROVIDER, new SerializeConfig(true).getProvider());
        assertSame(SerializeConfig.DEFAULT_PROVIDER, new SerializeConfig(false).getProvider());

        ObjectWriterCreator previous = JSONFactory.getContextWriterCreator();
        SerializeConfig config;
        try {
            JSONFactory.setContextWriterCreator(ObjectWriterCreator.INSTANCE);
            config = new SerializeConfig();
            assertSame(ObjectWriterCreator.INSTANCE, config.getProvider().getCreator());
        } finally {
            JSONFactory.setContextWriterCreator(previous);
        }
        ObjectWriterCreator creator = config.getProvider().getCreator();
        assertEquals(new ObjectWriterProvider().getCreator().getClass(), creator.getClass());
        if (creator instanceof ObjectWriterCreatorASM) {
            try {
                JSONFactory.setContextWriterCreator(ObjectWriterCreatorASM.INSTANCE);
                assertSame(ObjectWriterCreatorASM.INSTANCE, config.getProvider().getCreator());
            } finally {
                JSONFactory.setContextWriterCreator(previous);
            }
            assertSame(creator, config.getProvider().getCreator());
            assertNotSame(ObjectWriterCreatorASM.INSTANCE, creator);
        }
    }

    public static class Value {
        public int id = 7;
    }

    public static class Bean {
    }
}
