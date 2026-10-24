package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue7920 {
    @Test
    public void publicPathConvertsToString() {
        Child child = JSONObject.parseObject("{\"headers\":{\"k\":{}}}").toJavaObject(Child.class);
        assertEquals(String.class, child.getHeaders().get("k").getClass());
    }

    @Test
    public void bridgeSortsAfterRealSetter() {
        Method[] setters = setters();
        OrderedCreator creator = new OrderedCreator();
        FieldReader bridgeReader = creator.fieldReader(setters[0]);
        FieldReader realReader = creator.fieldReader(setters[1]);
        assertTrue(bridgeReader.compareTo(realReader) > 0);
        assertTrue(realReader.compareTo(bridgeReader) < 0);
    }

    @Test
    public void bridgeSetterFirst() {
        Method[] setters = setters();
        assertConverted(setters[0], setters[1]);
    }

    @Test
    public void realSetterFirst() {
        Method[] setters = setters();
        assertConverted(setters[1], setters[0]);
    }

    // the reflection order of getMethods() is not specified, so feed both orders explicitly
    private static Method[] setters() {
        Method bridge = null;
        Method real = null;
        for (Method method : Child.class.getDeclaredMethods()) {
            if (method.getName().equals("setHeaders")) {
                if (method.isBridge()) {
                    bridge = method;
                } else {
                    real = method;
                }
            }
        }
        assertNotNull(bridge);
        assertNotNull(real);
        return new Method[]{bridge, real};
    }

    private static void assertConverted(Method first, Method second) {
        Child child = new OrderedCreator().parse("{\"headers\":{\"k\":{}}}", first, second);
        Object value = child.getHeaders().get("k");
        assertEquals(String.class, value.getClass());
        assertEquals("{}", value);
    }

    static class OrderedCreator extends ObjectReaderCreator {
        private final ObjectReaderProvider provider = new ObjectReaderProvider();

        // a map holds one reader per field name, so each setter gets its own map to keep its reader
        FieldReader fieldReader(Method setter) {
            Map<String, List<FieldReader>> fields = new LinkedHashMap<>();
            addFieldReader(setter, fields);
            return fields.get("headers").get(0);
        }

        Child parse(String json, Method... setters) {
            Map<String, List<FieldReader>> fields = new LinkedHashMap<>();
            for (Method setter : setters) {
                addFieldReader(setter, fields);
            }
            FieldReader[] readers = fields.get("headers").toArray(new FieldReader[0]);
            provider.register(Child.class, createObjectReader(Child.class, readers));
            return JSON.parseObject(json, Child.class, new JSONReader.Context(provider));
        }

        private void addFieldReader(Method setter, Map<String, List<FieldReader>> fields) {
            FieldInfo fieldInfo = new FieldInfo();
            fieldInfo.init();
            createFieldReader(Child.class, Child.class, null, null, new BeanInfo(provider), fieldInfo, setter, fields, provider);
        }
    }

    public abstract static class Bean {
        protected Map<String, String> headers;

        public Map<String, String> getHeaders() {
            return headers;
        }

        public Bean setHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }
    }

    public static class Child extends Bean {
        @Override
        public Child setHeaders(Map<String, String> headers) {
            super.setHeaders(headers);
            return this;
        }
    }
}
