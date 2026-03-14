package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Java Record serialization and deserialization.
 */
public class RecordTest {
    // Simple record with primitive and String fields
    public record Point(int x, int y) {
    }

    // Record with String field
    public record Item(String name, int value) {
    }

    // Record with nested record
    public record Box(String label, Point origin) {
    }

    // Record with List field
    public record Container(String id, List<String> items) {
    }

    // Record with long and double
    public record Measurement(long timestamp, double value, String unit) {
    }

    // Record with boolean
    public record Feature(String name, boolean enabled) {
    }

    @Test
    public void testSerializeSimpleRecord() {
        Point p = new Point(3, 4);
        String json = JSON.toJSONString(p);
        assertTrue(json.contains("\"x\""));
        assertTrue(json.contains("\"y\""));
        assertTrue(json.contains("3"));
        assertTrue(json.contains("4"));
    }

    @Test
    public void testDeserializeSimpleRecord() {
        String json = "{\"x\":10,\"y\":20}";
        Point p = JSON.parseObject(json, Point.class);
        assertNotNull(p);
        assertEquals(10, p.x());
        assertEquals(20, p.y());
    }

    @Test
    public void testRoundTripRecord() {
        Item item = new Item("test", 42);
        byte[] bytes = JSON.toJSONBytes(item);
        Item parsed = JSON.parseObject(bytes, Item.class);
        assertNotNull(parsed);
        assertEquals("test", parsed.name());
        assertEquals(42, parsed.value());
    }

    @Test
    public void testNestedRecord() {
        Box box = new Box("myBox", new Point(1, 2));
        byte[] bytes = JSON.toJSONBytes(box);
        Box parsed = JSON.parseObject(bytes, Box.class);
        assertNotNull(parsed);
        assertEquals("myBox", parsed.label());
        assertNotNull(parsed.origin());
        assertEquals(1, parsed.origin().x());
        assertEquals(2, parsed.origin().y());
    }

    @Test
    public void testRecordWithList() {
        Container c = new Container("c1", List.of("a", "b", "c"));
        String json = JSON.toJSONString(c);
        Container parsed = JSON.parseObject(json, Container.class);
        assertNotNull(parsed);
        assertEquals("c1", parsed.id());
        assertNotNull(parsed.items());
        assertEquals(3, parsed.items().size());
        assertEquals("a", parsed.items().get(0));
    }

    @Test
    public void testRecordWithLongAndDouble() {
        Measurement m = new Measurement(1710000000000L, 3.14, "celsius");
        byte[] bytes = JSON.toJSONBytes(m);
        Measurement parsed = JSON.parseObject(bytes, Measurement.class);
        assertNotNull(parsed);
        assertEquals(1710000000000L, parsed.timestamp());
        assertEquals(3.14, parsed.value(), 0.001);
        assertEquals("celsius", parsed.unit());
    }

    @Test
    public void testRecordWithBoolean() {
        Feature f = new Feature("darkMode", true);
        String json = JSON.toJSONString(f);
        Feature parsed = JSON.parseObject(json, Feature.class);
        assertNotNull(parsed);
        assertEquals("darkMode", parsed.name());
        assertTrue(parsed.enabled());
    }

    @Test
    public void testDeserializeRecordFromBytes() {
        byte[] json = "{\"x\":100,\"y\":200}".getBytes();
        Point p = JSON.parseObject(json, Point.class);
        assertNotNull(p);
        assertEquals(100, p.x());
        assertEquals(200, p.y());
    }

    @Test
    public void testRecordFieldOrder() {
        // JSON fields in different order than record components
        String json = "{\"y\":5,\"x\":3}";
        Point p = JSON.parseObject(json, Point.class);
        assertNotNull(p);
        assertEquals(3, p.x());
        assertEquals(5, p.y());
    }

    @Test
    public void testRecordNullDeserialization() {
        String json = "null";
        Point p = JSON.parseObject(json, Point.class);
        assertNull(p);
    }
}
