package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.MonthDay;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Phase 2: Built-in type support via BuiltinCodecs.
 */
class BuiltinCodecsTest {
    private final ObjectMapper mapper = ObjectMapper.shared();

    // ==================== Optional ====================

    @Test
    void optionalWritePresent() {
        OptionalHolder h = new OptionalHolder();
        h.value = Optional.of("hello");
        String json = mapper.writeValueAsString(h);
        assertTrue(json.contains("\"hello\""), json);
    }

    @Test
    void optionalWriteEmpty() {
        OptionalHolder h = new OptionalHolder();
        h.value = Optional.empty();
        // The FieldWriter sees the Optional object (non-null), writes via ObjectWriter
        // which outputs null. With default config, null fields are written.
        String json = mapper.writeValueAsString(h);
        assertTrue(json.contains("\"value\""), json);
    }

    @Test
    void optionalIntRoundTrip() {
        String json = JSON.toJSONString(OptionalInt.of(42));
        assertEquals("42", json);
    }

    @Test
    void optionalLongRoundTrip() {
        String json = JSON.toJSONString(OptionalLong.of(123456789L));
        assertEquals("123456789", json);
    }

    @Test
    void optionalDoubleRoundTrip() {
        String json = JSON.toJSONString(OptionalDouble.of(3.14));
        assertEquals("3.14", json);
    }

    @Test
    void optionalIntEmpty() {
        String json = JSON.toJSONString(OptionalInt.empty());
        assertEquals("null", json);
    }

    // ==================== UUID ====================

    @Test
    void uuidRoundTrip() {
        UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        String json = JSON.toJSONString(uuid);
        assertEquals("\"550e8400-e29b-41d4-a716-446655440000\"", json);
    }

    @Test
    void uuidInPojo() {
        UUIDHolder h = new UUIDHolder();
        h.id = UUID.randomUUID();
        String json = mapper.writeValueAsString(h);
        assertTrue(json.contains(h.id.toString()), json);

        UUIDHolder h2 = mapper.readValue(json, UUIDHolder.class);
        assertEquals(h.id, h2.id);
    }

    // ==================== Duration ====================

    @Test
    void durationRoundTrip() {
        Duration d = Duration.ofHours(2).plusMinutes(30);
        String json = JSON.toJSONString(d);
        assertEquals("\"PT2H30M\"", json);
    }

    @Test
    void durationInPojo() {
        DurationHolder h = new DurationHolder();
        h.timeout = Duration.ofSeconds(90);
        String json = mapper.writeValueAsString(h);
        DurationHolder h2 = mapper.readValue(json, DurationHolder.class);
        assertEquals(h.timeout, h2.timeout);
    }

    // ==================== Period ====================

    @Test
    void periodRoundTrip() {
        Period p = Period.of(1, 6, 15);
        String json = JSON.toJSONString(p);
        assertEquals("\"P1Y6M15D\"", json);
    }

    @Test
    void periodInPojo() {
        PeriodHolder h = new PeriodHolder();
        h.retention = Period.ofMonths(3);
        String json = mapper.writeValueAsString(h);
        PeriodHolder h2 = mapper.readValue(json, PeriodHolder.class);
        assertEquals(h.retention, h2.retention);
    }

    // ==================== Year ====================

    @Test
    void yearRoundTrip() {
        Year y = Year.of(2026);
        String json = JSON.toJSONString(y);
        assertEquals("2026", json);
    }

    @Test
    void yearInPojo() {
        YearHolder h = new YearHolder();
        h.founded = Year.of(1999);
        String json = mapper.writeValueAsString(h);
        YearHolder h2 = mapper.readValue(json, YearHolder.class);
        assertEquals(h.founded, h2.founded);
    }

    // ==================== YearMonth ====================

    @Test
    void yearMonthRoundTrip() {
        YearMonth ym = YearMonth.of(2026, 3);
        String json = JSON.toJSONString(ym);
        assertEquals("\"2026-03\"", json);
    }

    @Test
    void yearMonthInPojo() {
        YearMonthHolder h = new YearMonthHolder();
        h.billing = YearMonth.of(2025, 12);
        String json = mapper.writeValueAsString(h);
        YearMonthHolder h2 = mapper.readValue(json, YearMonthHolder.class);
        assertEquals(h.billing, h2.billing);
    }

    // ==================== MonthDay ====================

    @Test
    void monthDayRoundTrip() {
        MonthDay md = MonthDay.of(12, 25);
        String json = JSON.toJSONString(md);
        assertEquals("\"--12-25\"", json);
    }

    @Test
    void monthDayInPojo() {
        MonthDayHolder h = new MonthDayHolder();
        h.birthday = MonthDay.of(3, 15);
        String json = mapper.writeValueAsString(h);
        MonthDayHolder h2 = mapper.readValue(json, MonthDayHolder.class);
        assertEquals(h.birthday, h2.birthday);
    }

    // ==================== URI ====================

    @Test
    void uriRoundTrip() {
        URI uri = URI.create("https://example.com/path?q=1");
        String json = JSON.toJSONString(uri);
        assertEquals("\"https://example.com/path?q=1\"", json);
    }

    @Test
    void uriInPojo() {
        URIHolder h = new URIHolder();
        h.endpoint = URI.create("https://api.example.com");
        String json = mapper.writeValueAsString(h);
        URIHolder h2 = mapper.readValue(json, URIHolder.class);
        assertEquals(h.endpoint, h2.endpoint);
    }

    // ==================== Path ====================

    @Test
    void pathRoundTrip() {
        Path p = Path.of("/tmp/test.json");
        String json = JSON.toJSONString(p);
        assertEquals("\"/tmp/test.json\"", json);
    }

    @Test
    void pathInPojo() {
        PathHolder h = new PathHolder();
        h.file = Path.of("/home/user/data.csv");
        String json = mapper.writeValueAsString(h);
        PathHolder h2 = mapper.readValue(json, PathHolder.class);
        assertEquals(h.file, h2.file);
    }

    // ==================== Test POJOs ====================

    public static class OptionalHolder {
        public Optional<String> value;
    }

    public static class UUIDHolder {
        public UUID id;
    }

    public static class DurationHolder {
        public Duration timeout;
    }

    public static class PeriodHolder {
        public Period retention;
    }

    public static class YearHolder {
        public Year founded;
    }

    public static class YearMonthHolder {
        public YearMonth billing;
    }

    public static class MonthDayHolder {
        public MonthDay birthday;
    }

    public static class URIHolder {
        public URI endpoint;
    }

    public static class PathHolder {
        public Path file;
    }

    // ==================== Optional<POJO> round-trip ====================

    @Test
    void optionalPojoRoundTrip() {
        OptionalPojoHolder h = new OptionalPojoHolder();
        UUIDHolder inner = new UUIDHolder();
        inner.id = UUID.randomUUID();
        h.item = Optional.of(inner);
        String json = mapper.writeValueAsString(h);
        assertTrue(json.contains(inner.id.toString()), json);
    }

    public static class OptionalPojoHolder {
        public Optional<UUIDHolder> item;
    }
}
