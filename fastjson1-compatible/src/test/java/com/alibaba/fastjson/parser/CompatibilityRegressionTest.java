package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.deserializer.MapDeserializer;
import com.alibaba.fastjson.serializer.StringCodec;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompatibilityRegressionTest {
    @Test
    public void typedMapDeserializer() {
        Type type = new TypeReference<TreeMap<Integer, Long>>() { }.getType();
        try (DefaultJSONParser parser = new DefaultJSONParser("{\"12\":34}")) {
            Map<Integer, Long> result = MapDeserializer.instance.deserialze(parser, type, null);
            assertTrue(result instanceof TreeMap);
            assertEquals(Long.valueOf(34), result.get(12));
        }
    }

    @Test
    public void parserRetainsInput() {
        String input = "{\"id\":123}";
        try (DefaultJSONParser parser = new DefaultJSONParser(input, ParserConfig.global)) {
            assertEquals(input, parser.getInput());
        }
    }

    @Test
    public void scannerWithFeaturesRetainsDateInput() {
        try (JSONScanner scanner = new JSONScanner("2020-01-02T03:04:05", 0)) {
            assertTrue(scanner.scanISO8601DateIfMatch());
        }
    }

    @Test
    public void orderedFieldFeatureCanBeDisabled() {
        try (JSONScanner scanner = new JSONScanner("{}", Feature.OrderedField.mask)) {
            assertTrue(scanner.isEnabled(Feature.OrderedField));
            scanner.config(Feature.OrderedField, false);
            assertFalse(scanner.isEnabled(Feature.OrderedField));
            scanner.config(Feature.OrderedField, true);
            assertTrue(scanner.isEnabled(Feature.OrderedField));
        }
    }

    @Test
    public void nullMutableStrings() {
        for (Class<?> type : new Class<?>[]{StringBuilder.class, StringBuffer.class}) {
            try (DefaultJSONParser parser = new DefaultJSONParser("null")) {
                assertNull(StringCodec.instance.deserialze(parser, type, null));
            }
        }
    }
}
