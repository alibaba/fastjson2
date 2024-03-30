package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2367 {
    @Data
    public static final class AttributeKey<T extends Serializable>
            implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String name;

        public AttributeKey(final String name) {
            this.name = name;
        }
    }

    @Data
    @Slf4j
    public static final class PubSubAttributes
            implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Map<AttributeKey<?>, Serializable> attributes;

        public PubSubAttributes() {
            this(new ConcurrentHashMap<>());
        }

        PubSubAttributes(Map<AttributeKey<?>, Serializable> attributes) {
            this.attributes = attributes;
        }

        public <T extends Serializable> T setAttribute(
                final @NonNull AttributeKey<T> attributeKey,
                final @NonNull T value) {
            return (T) attributes.put(attributeKey, value);
        }

        public <T extends Serializable> T getAttribute(final @NonNull AttributeKey<T> attributeKey) {
            return (T) attributes.get(attributeKey);
        }
    }

    @Data
    public static final class Subscription {
        private PubSubAttributes attributes;
    }

    @Test
    @SuppressWarnings("deprecated")
    public void testWriteClassName() {
        final Filter filter = JSONReader.autoTypeFilter(
                true,
                "com.alibaba.fastjson2.", "java.");

        final PubSubAttributes attributes = new PubSubAttributes();
        final AttributeKey<String> APP_KEY = new AttributeKey<>("appKey");
        attributes.setAttribute(APP_KEY, "123456");
        //
        final Subscription subscription = new Subscription();
        subscription.setAttributes(attributes);

        final String jsonStr = JSON.toJSONString(subscription, JSONWriter.Feature.WriteClassName);

        System.out.println(jsonStr);
        //works
        //final Subscription parsedSubscription = JSON.parseObject(jsonStr, Subscription.class, SupportAutoType);
        //does not work
        final Subscription parsedSubscription = JSON.parseObject(jsonStr, Subscription.class, filter);
        PubSubAttributes attributes1 = parsedSubscription.getAttributes();
        assertEquals(1, attributes1.attributes.size());
        assertEquals(AttributeKey.class, attributes1.attributes.entrySet().iterator().next().getKey().getClass());
        assertEquals("123456", attributes1.getAttribute(APP_KEY));
    }
}
