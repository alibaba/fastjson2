package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for java.time.* and java.util.Date serialization/deserialization.
 */
class TemporalTest {
    public static class Event {
        public String name;
        public LocalDate date;
        public LocalDateTime dateTime;
        public LocalTime time;
        public Instant instant;

        public Event() {
        }
    }

    public static class EventWithZoned {
        public String name;
        public ZonedDateTime zoned;
        public OffsetDateTime offset;
        public Date legacyDate;

        public EventWithZoned() {
        }
    }

    @Test
    void testSerializeLocalDate() {
        Event event = new Event();
        event.name = "meeting";
        event.date = LocalDate.of(2024, 6, 15);
        String json = JSON.toJSONString(event);
        assertNotNull(json);
        assertTrue(json.contains("\"2024-06-15\""), json);
    }

    @Test
    void testSerializeLocalDateTime() {
        Event event = new Event();
        event.name = "meeting";
        event.dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
        String json = JSON.toJSONString(event);
        assertNotNull(json);
        assertTrue(json.contains("\"2024-06-15T10:30\""), json);
    }

    @Test
    void testSerializeInstant() {
        Event event = new Event();
        event.name = "alarm";
        event.instant = Instant.parse("2024-06-15T10:30:00Z");
        String json = JSON.toJSONString(event);
        assertNotNull(json);
        assertTrue(json.contains("\"2024-06-15T10:30:00Z\""), json);
    }

    @Test
    void testSerializeLocalTime() {
        Event event = new Event();
        event.name = "alarm";
        event.time = LocalTime.of(14, 30, 0);
        String json = JSON.toJSONString(event);
        assertNotNull(json);
        assertTrue(json.contains("\"14:30\""), json);
    }

    @Test
    void testSerializeDate() {
        EventWithZoned event = new EventWithZoned();
        event.name = "legacy";
        event.legacyDate = Date.from(Instant.parse("2024-06-15T10:30:00Z"));
        String json = JSON.toJSONString(event);
        assertNotNull(json);
        assertTrue(json.contains("\"2024-06-15T10:30:00Z\""), json);
    }

    @Test
    void testSerializeZonedDateTime() {
        EventWithZoned event = new EventWithZoned();
        event.name = "flight";
        event.zoned = ZonedDateTime.of(2024, 6, 15, 10, 30, 0, 0, ZoneOffset.UTC);
        String json = JSON.toJSONString(event);
        assertNotNull(json);
        assertTrue(json.contains("2024-06-15T10:30"), json);
    }

    @Test
    void testDeserializeLocalDate() {
        String json = "{\"name\":\"meeting\",\"date\":\"2024-06-15\"}";
        ObjectMapper mapper = ObjectMapper.shared();
        Event event = mapper.readValue(json, Event.class);
        assertEquals("meeting", event.name);
        assertEquals(LocalDate.of(2024, 6, 15), event.date);
    }

    @Test
    void testDeserializeLocalDateTime() {
        String json = "{\"name\":\"meeting\",\"dateTime\":\"2024-06-15T10:30:00\"}";
        ObjectMapper mapper = ObjectMapper.shared();
        Event event = mapper.readValue(json, Event.class);
        assertEquals("meeting", event.name);
        assertEquals(LocalDateTime.of(2024, 6, 15, 10, 30, 0), event.dateTime);
    }

    @Test
    void testDeserializeInstant() {
        String json = "{\"name\":\"alarm\",\"instant\":\"2024-06-15T10:30:00Z\"}";
        ObjectMapper mapper = ObjectMapper.shared();
        Event event = mapper.readValue(json, Event.class);
        assertEquals("alarm", event.name);
        assertEquals(Instant.parse("2024-06-15T10:30:00Z"), event.instant);
    }

    @Test
    void testDeserializeLocalTime() {
        String json = "{\"name\":\"alarm\",\"time\":\"14:30:00\"}";
        ObjectMapper mapper = ObjectMapper.shared();
        Event event = mapper.readValue(json, Event.class);
        assertEquals("alarm", event.name);
        assertEquals(LocalTime.of(14, 30, 0), event.time);
    }

    @Test
    void testDeserializeDate() {
        String json = "{\"name\":\"legacy\",\"legacyDate\":\"2024-06-15T10:30:00Z\"}";
        ObjectMapper mapper = ObjectMapper.shared();
        EventWithZoned event = mapper.readValue(json, EventWithZoned.class);
        assertEquals("legacy", event.name);
        assertEquals(Date.from(Instant.parse("2024-06-15T10:30:00Z")), event.legacyDate);
    }

    @Test
    void testRoundTripLocalDate() {
        Event event = new Event();
        event.name = "test";
        event.date = LocalDate.of(2024, 1, 1);
        event.dateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        event.time = LocalTime.of(12, 0, 0);
        event.instant = Instant.parse("2024-01-01T12:00:00Z");

        String json = JSON.toJSONString(event);
        Event deserialized = ObjectMapper.shared().readValue(json, Event.class);

        assertEquals(event.name, deserialized.name);
        assertEquals(event.date, deserialized.date);
        assertEquals(event.dateTime, deserialized.dateTime);
        assertEquals(event.time, deserialized.time);
        assertEquals(event.instant, deserialized.instant);
    }

    @Test
    void testNullTemporalFields() {
        Event event = new Event();
        event.name = "empty";
        String json = JSON.toJSONString(event);
        // Null temporal fields should be omitted by default
        assertFalse(json.contains("date"), json);
        assertFalse(json.contains("dateTime"), json);

        Event deserialized = ObjectMapper.shared().readValue(json, Event.class);
        assertEquals("empty", deserialized.name);
        assertNull(deserialized.date);
        assertNull(deserialized.dateTime);
    }
}
