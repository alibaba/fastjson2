package com.alibaba.fastjson.support.spring;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class GenericFastJsonRedisSerializerAcceptListTest {
    static final String[] ACCEPT_NAMES = {Bean.class.getName()};

    @Test
    public void rejectTypeOutsideAcceptList() {
        Gadget.SETTER_CALLS.set(0);
        GenericFastJsonRedisSerializer serializer = new GenericFastJsonRedisSerializer(ACCEPT_NAMES);
        byte[] bytes = ("{\"@type\":\"" + Gadget.class.getName() + "\",\"value\":\"x\"}")
                .getBytes(StandardCharsets.UTF_8);
        Object result = serializer.deserialize(bytes);
        assertFalse(result instanceof Gadget);
        assertEquals(0, Gadget.SETTER_CALLS.get());
    }

    @Test
    public void acceptedTypeRoundTrip() {
        GenericFastJsonRedisSerializer serializer = new GenericFastJsonRedisSerializer(ACCEPT_NAMES);
        Bean bean = new Bean();
        bean.id = 1;
        bean.items = java.util.Arrays.asList(1L, "a");
        bean.attrs = new HashMap<>();
        bean.attrs.put("k", 2L);
        bean.labels = new HashMap<>();
        bean.labels.put("p", "q");
        Map<String, Object> root = new HashMap<>();
        root.put("bean", bean);

        Map<?, ?> map = (Map<?, ?>) serializer.deserialize(serializer.serialize(root));
        Bean result = (Bean) map.get("bean");
        assertEquals(1, result.id);
        assertEquals(bean.items, result.items);
        assertEquals(bean.attrs, result.attrs);
        assertEquals(bean.labels, result.labels);
    }

    public static class Bean {
        public int id;
        public List<Object> items;
        public Map<String, Object> attrs;
        public Map<String, String> labels;
    }

    public static class Gadget {
        static final AtomicInteger SETTER_CALLS = new AtomicInteger();
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            SETTER_CALLS.incrementAndGet();
            this.value = value;
        }
    }
}
