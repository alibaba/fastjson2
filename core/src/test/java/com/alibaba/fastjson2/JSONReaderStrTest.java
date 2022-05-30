package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JSONReaderStrTest {
    @Test
    public void test_0() {
        String str = "{id:123,\"Name\":\"DataWorks\"}";
        JSONReader reader = new JSONReaderStr(JSONFactory.createReadContext(), str, 0, str.length());
        assertTrue(
                reader.nextIfObjectStart()
        );

        assertEquals(
                Fnv.hashCode64("id"),
                reader.readFieldNameHashCodeUnquote()
        );

        assertEquals(123,
                reader.readInt32()
                        .intValue());

        reader.skipName();
        assertEquals(
                Fnv.hashCode64("DataWorks"),
                reader.readValueHashCode()
        );
    }

    @Test
    public void test_UUID() {
        UUID uuid = UUID.randomUUID();
        String str = "{\"id\":\"" + uuid + "\",\"count\":1238}";
        JSONReader reader = new JSONReaderStr(JSONFactory.createReadContext(), str, 0, str.length());

        assertTrue(
                reader.nextIfObjectStart()
        );

        assertEquals(
                Fnv.hashCode64("id"),
                reader.readFieldNameHashCode()
        );

        assertEquals(
                uuid,
                reader.readUUID()
        );

        assertFalse(
                reader.nextIfMatch(',')
        );

        assertEquals(
                Fnv.hashCode64("count"),
                reader.readFieldNameHashCode()
        );

        assertEquals(1238,
                reader.readInt64()
                        .intValue());
    }

    @Test
    public void test1_str() {
        String str = "\"2022-05-10T11:07Z[UTC]\"";
        JSONReader reader = new JSONReaderStr(JSONFactory.createReadContext(), str, 0, str.length());

        Instant instant = reader.readInstant();
        assertNotNull(instant);
    }

    @Test
    public void test2_str() {
        String str = "{\"type\":\"APP_SYNC\",\"appList\":[{\"appPath\":\"C:\\\\Users\\\\apple\\\\AppData\"}]}";
        JSONReader reader = new JSONReaderStr(JSONFactory.createReadContext(), str, 0, str.length());
        assertTrue(JSONValidator.from(reader).validate());
    }
}
