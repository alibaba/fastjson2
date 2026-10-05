package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONSchemaValidException;
import com.alibaba.fastjson2.schema.JSONSchema;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValueReaderSchemaTest {
    @Test
    public void integerSchemaIsEnforcedBeforeCreator() {
        AtomicInteger calls = new AtomicInteger();
        ObjectReader<Integer> reader = ObjectReaderImplValueInt.of(Integer.class, 0,
                JSONSchema.of(JSONObject.of("type", "integer", "minimum", 10)), value -> {
                    calls.incrementAndGet();
                    return value;
                });
        try (JSONReader json = JSONReader.of("9")) {
            assertThrows(JSONSchemaValidException.class, () -> reader.readObject(json));
        }
        try (JSONReader json = JSONReader.ofJSONB(JSONB.toBytes(9))) {
            assertThrows(JSONSchemaValidException.class, () -> reader.readJSONBObject(json, null, null, 0));
        }
        assertEquals(0, calls.get());
        try (JSONReader json = JSONReader.of("10")) {
            assertEquals(Integer.valueOf(10), reader.readObject(json));
        }
        assertEquals(1, calls.get());
    }

    @Test
    public void stringSchemaIsEnforcedBeforeCreator() {
        AtomicInteger calls = new AtomicInteger();
        ObjectReader<String> reader = ObjectReaderImplValueString.of(String.class, 0,
                JSONSchema.of(JSONObject.of("type", "string", "minLength", 2)), value -> {
                    calls.incrementAndGet();
                    return value;
                });
        try (JSONReader json = JSONReader.of("\"a\"")) {
            assertThrows(JSONSchemaValidException.class, () -> reader.readObject(json));
        }
        try (JSONReader json = JSONReader.ofJSONB(JSONB.toBytes("a"))) {
            assertThrows(JSONSchemaValidException.class, () -> reader.readJSONBObject(json, null, null, 0));
        }
        assertEquals(0, calls.get());
        try (JSONReader json = JSONReader.of("\"ab\"")) {
            assertEquals("ab", reader.readObject(json));
        }
        assertEquals(1, calls.get());
    }

    @Test
    public void genericSchemaIsEnforcedBeforeCreator() {
        AtomicInteger calls = new AtomicInteger();
        ObjectReader<String> reader = new ObjectReaderImplValue<>(String.class, String.class, String.class,
                0, null, null, JSONSchema.of(JSONObject.of("type", "string", "minLength", 2)),
                null, null, value -> {
                    calls.incrementAndGet();
                    return value;
                });
        try (JSONReader json = JSONReader.of("\"a\"")) {
            assertThrows(JSONSchemaValidException.class, () -> reader.readObject(json));
        }
        try (JSONReader json = JSONReader.ofJSONB(JSONB.toBytes("a"))) {
            assertThrows(JSONSchemaValidException.class, () -> reader.readJSONBObject(json, null, null, 0));
        }
        assertEquals(0, calls.get());
        try (JSONReader json = JSONReader.of("\"ab\"")) {
            assertEquals("ab", reader.readObject(json));
        }
        assertEquals(1, calls.get());
    }

    @Test
    public void stringArrayReaderReportsItsActualType() {
        ObjectReader reader = new ObjectReaderProvider().getObjectReader(String[].class);
        assertEquals(String[].class, reader.getObjectClass());
    }
}
