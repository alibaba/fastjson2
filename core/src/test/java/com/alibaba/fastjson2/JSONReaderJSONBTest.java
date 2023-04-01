package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JSONReaderJSONBTest {
    @Test
    public void getString() {
        String[] strings = new String[]{
                "abc",
                "abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890",
                "中文", "发展特色富民产业，扎扎实实把乡村振兴战略实施好"
        };
        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            jsonReader.readValueHashCode();
            assertEquals(string, jsonReader.getString());
        }

        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string, StandardCharsets.UTF_16);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            jsonReader.readValueHashCode();
            assertEquals(string, jsonReader.getString());
        }

        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string, StandardCharsets.UTF_16LE);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            jsonReader.readValueHashCode();
            assertEquals(string, jsonReader.getString());
        }

        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string, StandardCharsets.UTF_16BE);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            jsonReader.readValueHashCode();
            assertEquals(string, jsonReader.getString());
        }
    }

    @Test
    public void readFieldName() {
        String[] strings = new String[]{
                "abc",
                "abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890",
                "中文", "发展特色富民产业，扎扎实实把乡村振兴战略实施好"
        };
        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            assertEquals(string, jsonReader.readFieldName());
        }

        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string, StandardCharsets.UTF_16);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            assertEquals(string, jsonReader.readFieldName());
        }

        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string, StandardCharsets.UTF_16LE);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            assertEquals(string, new String(jsonReader.readFieldName().toCharArray()));
        }

        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string, StandardCharsets.UTF_16BE);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            assertEquals(string, new String(jsonReader.readFieldName().toCharArray()));
        }

        {
            SymbolTable symbolTable = JSONB.symbolTable("id");
            JSONWriter jsonWriter = JSONWriter.ofJSONB(symbolTable);
            jsonWriter.writeSymbol("id");
            byte[] bytes = jsonWriter.getBytes();
            JSONReader jsonReader = JSONReader.ofJSONB(bytes, 0, bytes.length, symbolTable);
            assertEquals("id", jsonReader.readFieldName());
        }
    }

    @Test
    public void readFieldNameHashCodeUnquote() {
        byte[] bytes = JSONB.toBytes("id");
        JSONReader jsonReader = JSONReader.ofJSONB(bytes);
        assertEquals(Fnv.hashCode64("id"), jsonReader.readFieldNameHashCodeUnquote());
        assertFalse(jsonReader.nextIfSet());
    }

    @Test
    public void notSupported() {
        JSONReaderJSONB jsonReader = (JSONReaderJSONB) JSONReader.ofJSONB(JSONB.toBytes(""));
        assertThrows(JSONException.class, () -> jsonReader.nextIfMatch('A'));
        assertThrows(JSONException.class, () -> jsonReader.readNullOrNewDate());
        assertThrows(JSONException.class, () -> jsonReader.readNumber0());
        assertThrows(JSONException.class, () -> jsonReader.readLocalDateTime16());
        assertThrows(JSONException.class, () -> jsonReader.readLocalDateTime17());
        assertThrows(JSONException.class, () -> jsonReader.readLocalTime10());
        assertThrows(JSONException.class, () -> jsonReader.readLocalTime11());
        assertThrows(JSONException.class, () -> jsonReader.readLocalDate11());
        assertThrows(JSONException.class, () -> jsonReader.readLocalDateTime18());
        assertThrows(JSONException.class, () -> jsonReader.readZonedDateTimeX(10));
        assertThrows(JSONException.class, () -> jsonReader.skipLineComment());
        assertThrows(JSONException.class, () -> jsonReader.readPattern());
        assertThrows(JSONException.class, () -> jsonReader.nextIfMatchIdent('0', '1', '2'));
        assertThrows(JSONException.class, () -> jsonReader.nextIfMatchIdent('0', '1', '2', '3'));
        assertThrows(JSONException.class, () -> jsonReader.nextIfMatchIdent('0', '1', '2', '3', '4'));
        assertThrows(JSONException.class, () -> jsonReader.nextIfMatchIdent('0', '1', '2', '3', '4', '5'));
    }

    @Test
    public void test1() {
        byte[] bytes = new byte[32];
        new Random().nextBytes(bytes);
        String base64 = Base64.getEncoder().encodeToString(bytes);
        byte[] jsonbBytes = JSONObject.of("value", base64).toJSONBBytes();
        Bean1 bean1 = JSONB.parseObject(jsonbBytes, Bean1.class);
        assertArrayEquals(bytes, bean1.value);
    }

    public static class Bean1 {
        public byte[] value;
    }

    @Test
    public void testReadHex() {
        String json = "{\"value\":\"1EA7CEB8DF5BAF501A33F86AD12B74E14FC3A889513E787CE709E7DBA4A2B31E\"}";
        Bean2 bean = JSON.parseObject(json, Bean2.class);
        byte[] jsonbBytes = JSON.parseObject(json).toJSONBBytes();
//        System.out.println(JSONB.toJSONString(jsonbBytes));
        Bean2 bean1 = JSONB.parseObject(jsonbBytes, Bean2.class);
        assertArrayEquals(bean.value, bean1.value);
    }

    public static class Bean2 {
        @JSONField(format = "hex")
        public byte[] value;
    }

    @Test
    public void readCharValue() {
        char[] chars = new char[]{'a', '0', '中', '\0'};
        for (char ch : chars) {
            byte[] jsonbBytes = JSONObject.of("value", Character.toString(ch)).toJSONBBytes();
            Bean3 bean = JSONB.parseObject(jsonbBytes, Bean3.class);
            assertEquals(ch, bean.value);
        }
    }

    @Test
    public void readCharValue1() {
        byte[] jsonbBytes = JSONObject.of("value", "").toJSONBBytes();
        System.out.println(JSONB.toJSONString(jsonbBytes));
        assertEquals(
                '\0',
                JSONB.parseObject(
                        jsonbBytes,
                        Bean3.class
                ).value
        );
    }

    @Test
    public void readCharValue2() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.startObject();
        jsonWriter.writeName("value");
        jsonWriter.writeRaw(JSONB.toBytes("", StandardCharsets.UTF_8));
        jsonWriter.endObject();

        byte[] jsonbBytes = jsonWriter.getBytes();
        System.out.println(JSONB.toJSONString(jsonbBytes));
        assertEquals(
                '\0',
                JSONB.parseObject(
                        jsonbBytes,
                        Bean3.class
                ).value
        );
    }

    public static class Bean3 {
        public char value;
    }

    @Test
    public void readLocalTime() {
        byte[] jsonBytes = JSONB.toBytes("12:13");
        LocalTime localTime = JSONB.parseObject(jsonBytes, LocalTime.class);
        assertEquals(12, localTime.getHour());
        assertEquals(13, localTime.getMinute());
        assertEquals(0, localTime.getSecond());
    }

    @Test
    public void readLocalDateTime() {
        byte[] jsonBytes = JSONB.toBytes("2017/05/06T12:13:14");
        LocalDateTime localTime = JSONB.parseObject(jsonBytes, LocalDateTime.class);
        assertEquals(2017, localTime.getYear());
        assertEquals(5, localTime.getMonthValue());
        assertEquals(6, localTime.getDayOfMonth());

        assertEquals(12, localTime.getHour());
        assertEquals(13, localTime.getMinute());
        assertEquals(14, localTime.getSecond());
    }

    @Test
    public void readBoolValue() {
        assertTrue(
                JSONReader.ofJSONB(
                        JSONB.toBytes("true")
                ).readBoolValue()
        );
        assertTrue(
                JSONReader.ofJSONB(
                        JSONB.toBytes("TRUE")
                ).readBoolValue()
        );
        assertTrue(
                JSONReader.ofJSONB(
                        JSONB.toBytes("Y")
                ).readBoolValue()
        );
        assertTrue(
                JSONReader.ofJSONB(
                        JSONB.toBytes("Y", StandardCharsets.UTF_8)
                ).readBoolValue()
        );
        assertTrue(
                JSONReader.ofJSONB(
                        JSONB.toBytes("Y", StandardCharsets.UTF_16)
                ).readBoolValue()
        );
        assertTrue(
                JSONReader.ofJSONB(
                        JSONB.toBytes("TRUE", StandardCharsets.UTF_16)
                ).readBoolValue()
        );
        assertTrue(
                JSONReader.ofJSONB(
                        JSONB.toBytes("true", StandardCharsets.UTF_16)
                ).readBoolValue()
        );
        assertFalse(
                JSONReader.ofJSONB(
                        JSONB.toBytes("false", StandardCharsets.UTF_8)
                ).readBoolValue()
        );
        assertFalse(
                JSONReader.ofJSONB(
                        JSONB.toBytes("FALSE", StandardCharsets.UTF_16)
                ).readBoolValue()
        );
    }

    @Test
    public void readUUID() {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        String str1 = str.replaceAll("-", "");
        assertEquals(
                uuid,
                JSONReader.ofJSONB(
                        JSONB.toBytes(str)
                ).readUUID()
        );
        assertEquals(
                uuid,
                JSONReader.ofJSONB(
                        JSONB.toBytes(str, StandardCharsets.UTF_8)
                ).readUUID()
        );
        assertEquals(
                uuid,
                JSONReader.ofJSONB(
                        JSONB.toBytes(str1, StandardCharsets.UTF_8)
                ).readUUID()
        );
    }

    @Test
    public void readZonedDateTime() {
        Date date = new Date();
        Instant instant = date.toInstant();
        ZonedDateTime zdt = instant.atZone(DateUtils.DEFAULT_ZONE_ID);
        assertEquals(
                zdt,
                JSONReader.ofJSONB(
                        JSONB.toBytes(instant)
                ).readZonedDateTime()
        );
        assertEquals(
                instant,
                JSONReader.ofJSONB(
                        JSONB.toBytes(instant)
                ).readInstant()
        );
        assertEquals(
                zdt,
                JSONReader.ofJSONB(
                        JSONB.toBytes(date)
                ).readZonedDateTime()
        );
        assertEquals(
                instant,
                JSONReader.ofJSONB(
                        JSONB.toBytes(date)
                ).readInstant()
        );
    }

    @Test
    public void readZonedDateTime1() {
        Date date = new Date(1680363962000L);
        Instant instant = date.toInstant();
        ZonedDateTime zdt = instant.atZone(DateUtils.DEFAULT_ZONE_ID);
        assertEquals(
                zdt,
                JSONReader.ofJSONB(
                        JSONB.toBytes(instant)
                ).readZonedDateTime()
        );
        assertEquals(
                instant,
                JSONReader.ofJSONB(
                        JSONB.toBytes(instant)
                ).readInstant()
        );
        assertEquals(
                zdt,
                JSONReader.ofJSONB(
                        JSONB.toBytes(date)
                ).readZonedDateTime()
        );
        assertEquals(
                instant,
                JSONReader.ofJSONB(
                        JSONB.toBytes(date)
                ).readInstant()
        );
    }

    @Test
    public void readZonedDateTime2() {
        Date date = new Date(6680363940000L);
        Instant instant = date.toInstant();
        ZonedDateTime zdt = instant.atZone(DateUtils.DEFAULT_ZONE_ID);
        assertEquals(
                zdt,
                JSONReader.ofJSONB(
                        JSONB.toBytes(instant)
                ).readZonedDateTime()
        );
        assertEquals(
                zdt,
                JSONReader.ofJSONB(
                        JSONB.toBytes(date)
                ).readZonedDateTime()
        );
        assertEquals(
                instant,
                JSONReader.ofJSONB(
                        JSONB.toBytes(date)
                ).readInstant()
        );
    }

    @Test
    public void mark() {
        byte[] jsonBytes = JSONB.toBytes("12:13");
        JSONReader jsonReader = JSONReader.ofJSONB(jsonBytes);
        jsonReader.reset(jsonReader.mark());
    }
}
