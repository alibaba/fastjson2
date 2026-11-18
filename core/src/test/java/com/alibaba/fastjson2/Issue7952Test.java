package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue7952Test {
    static final String TYPE_NAME = Issue7952Test.Payload.class.getName();

    ObjectReaderProvider provider;
    JSONReader.AutoTypeBeforeHandler savedHandler;

    static class Payload {
        public String id;
    }

    @BeforeEach
    public void setUp() {
        provider = JSONFactory.getDefaultObjectReaderProvider();
        savedHandler = provider.getAutoTypeBeforeHandler();
        provider.setAutoTypeBeforeHandler(
                (typeName, expectClass, features) -> TYPE_NAME.equals(typeName) ? Payload.class : null);
    }

    @AfterEach
    public void tearDown() {
        provider.setAutoTypeBeforeHandler(savedHandler);
    }

    JSONReader.Context newContext() {
        JSONReader.Context context = new JSONReader.Context(provider);
        context.config(JSONReader.Feature.ErrorOnNotSupportAutoType);
        return context;
    }

    String json() {
        return "[{\"" + "@type\":\"" + TYPE_NAME + "\",\"id\":\"a1\"}]";
    }

    @Test
    public void testParseStringConsultsHandler() {
        Object object = JSON.parse(json(), newContext());

        assertTrue(object instanceof List);
        Object item = ((List<?>) object).get(0);
        assertTrue(item instanceof Payload);
        assertEquals("a1", ((Payload) item).id);
    }

    @Test
    public void testParseBytesConsultsHandler() {
        Object object = JSON.parse(json().getBytes(StandardCharsets.UTF_8), newContext());

        assertTrue(object instanceof List);
        Object item = ((List<?>) object).get(0);
        assertTrue(item instanceof Payload);
        assertEquals("a1", ((Payload) item).id);
    }

    @Test
    public void testUnauthorizedTypeStillRejected() {
        provider.setAutoTypeBeforeHandler(null);

        String other = "[{\"" + "@type\":\"" + Payload.class.getSuperclass().getName() + "\"}]";
        assertThrows(JSONException.class, () -> JSON.parse(other, newContext()));
        assertThrows(JSONException.class, () -> JSON.parse(other.getBytes(StandardCharsets.UTF_8), newContext()));
    }
}
