package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class JSONWriterJSONBTest {
    @Test
    public void test_startObject() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        for (int i = 0; i < 8096; i++) {
            jsonWriter.startObject();
            jsonWriter.endObject();
        }
    }

    @Test
    public void test_startArray() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        for (int i = 0; i < 8096; i++) {
            jsonWriter.startArray(1);
        }
    }

    @Test
    public void test_writeRaw() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeRaw(JSONB.Constants.BC_NULL);
        }
    }

    @Test
    public void test_writeRaw_1() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeRaw(new byte[]{JSONB.Constants.BC_NULL});
        }
    }

    @Test
    public void test_writeMillis() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeMillis(1);
        }
    }

    @Test
    public void notSupported() {
        JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
        assertThrows(JSONException.class, () -> jsonWriter.startArray());
        assertThrows(JSONException.class, () -> jsonWriter.writeRaw(""));
        assertThrows(JSONException.class, () -> jsonWriter.writeComma());
        assertThrows(JSONException.class, () -> jsonWriter.write0('A'));
        assertThrows(JSONException.class, () -> jsonWriter.writeDateTimeISO8601(2001, 1, 1, 12, 13, 14, 0, 0, true));
        assertThrows(JSONException.class, () -> jsonWriter.writeDateYYYMMDD10(2001, 1, 1));
        assertThrows(JSONException.class, () -> jsonWriter.writeTimeHHMMSS8(12, 13, 14));
        assertThrows(JSONException.class, () -> jsonWriter.writeBase64(new byte[0]));
        assertThrows(JSONException.class, () -> jsonWriter.writeRaw('A'));
        assertThrows(JSONException.class, () -> jsonWriter.writeNameRaw(new byte[0], 0, 0));
        assertThrows(JSONException.class, () -> jsonWriter.writeNameRaw(new char[0]));
        assertThrows(JSONException.class, () -> jsonWriter.writeNameRaw(new char[0], 0, 0));
        assertThrows(JSONException.class, () -> jsonWriter.writeColon());
        assertThrows(JSONException.class, () -> jsonWriter.flushTo(null, null));
    }

    @Test
    public void writeDateTime19() {
        JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
        jsonWriter.writeDateTime19(2013, 5, 6, 12, 13, 14);
        assertEquals("\"2013-05-06 12:13:14\"", JSONB.toJSONString(jsonWriter.getBytes()));
    }

    @Test
    public void writeString() {
        JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
        jsonWriter.writeChar('A');
        assertEquals("\"A\"", JSONB.toJSONString(jsonWriter.getBytes()));
    }

    @Test
    public void startArray() {
        Integer[] array = new Integer[]{1, 2, 3};
        JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
        jsonWriter.startArray(array, array.length);
        for (Integer item : array) {
            jsonWriter.writeInt32(item);
        }
        assertEquals("[\n" +
                "\t1,\n" +
                "\t2,\n" +
                "\t3\n" +
                "]", JSONB.toJSONString(jsonWriter.getBytes()));
    }

    @Test
    public void capacity() throws Exception {
        Field bytes = JSONWriterJSONB.class.getDeclaredField("bytes");
        bytes.setAccessible(true);

        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.startArray(new Object[1], 27);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.endObject();
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeAny(null);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeString((char[]) null);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[1]);
            jsonWriter.writeString(new char[0]);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[1]);
            char[] chars = "01234567890".toCharArray();
            jsonWriter.writeString(chars, 5, 0);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeTypeName("abc");
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[1]);
            jsonWriter.writeTypeName("abc");

            byte[] bytes1 = (byte[]) bytes.get(jsonWriter);
            bytes.set(jsonWriter, Arrays.copyOf(bytes1, 7));
            jsonWriter.writeTypeName("abc");

            bytes.set(jsonWriter, new byte[1]);
            jsonWriter.off = 0;
            jsonWriter.writeTypeName("abc");

            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.off = 0;
            jsonWriter.ensureCapacity(1);
        }

        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeMillis(1000);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeMillis(214700 * 3600L * 6000L);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt64(1);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt64(64);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt64(262143);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt64(Integer.MAX_VALUE);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt64(new long[1]);
            jsonWriter.writeInt64(null);
            jsonWriter.writeFloat((Float) null);
            jsonWriter.writeFloat((float[]) null);
            jsonWriter.writeDouble((double[]) null);
            jsonWriter.writeInt32(null);
            jsonWriter.writeLocalDate(null);
            jsonWriter.writeLocalTime(null);
            jsonWriter.writeLocalDateTime(null);
            jsonWriter.writeZonedDateTime(null);
            jsonWriter.writeInstant(null);
            jsonWriter.writeUUID(null);
            jsonWriter.writeBigInt(null);
            jsonWriter.writeBinary(null);
            jsonWriter.writeDecimal(null);
            jsonWriter.writeBool(null);
            jsonWriter.writeBigInt(null, 0);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt64(new long[]{64});
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt64(new long[]{262143});
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt64(new long[]{Integer.MAX_VALUE});
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt32(new int[]{8});
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt32(new int[]{64});
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt32(new int[]{262143});
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt32(new int[]{Integer.MAX_VALUE});
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt8((byte) 1);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt16((short) 1);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt32(1);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt32(64);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt32(262143);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeInt32(Integer.MAX_VALUE);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeArrayNull();
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeBigInt(BigInteger.ONE);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeBool(true);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeReference("$");
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB();
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeTypeName(new byte[1], 1);
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.off = 0;
            jsonWriter.writeTypeName(new byte[1], 1);
        }
        {
            JSONWriterJSONB jsonWriter = (JSONWriterJSONB) JSONWriter.ofJSONB(JSONB.symbolTable("id"));
            bytes.set(jsonWriter, new byte[0]);
            jsonWriter.writeTypeName(new byte[1], Fnv.hashCode64("id"));
        }
    }

    @Test
    public void sizeOfInt() {
        assertEquals(5, JSONWriterJSONB.sizeOfInt(Integer.MAX_VALUE));
    }

    @Test
    public void testInstant() {
        Bean bean = new Bean();
        bean.date = Instant.ofEpochMilli(1679826319000L);

        byte[] bytes = JSONB.toBytes(bean);
        System.out.println(JSONB.toJSONString(bytes));
        Bean bean1 = JSONB.parseObject(bytes, Bean.class);
        assertEquals(bean.date.toEpochMilli(), bean1.date.toEpochMilli());
    }

    @Test
    public void writeDateTime14() {
        LocalDateTime ldt = LocalDateTime.of(2023, 3, 26, 10, 25, 19);
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeDateTime14(
                ldt.getYear(),
                ldt.getMonthValue(),
                ldt.getDayOfMonth(),
                ldt.getHour(),
                ldt.getMinute(),
                ldt.getSecond()
        );

        byte[] bytes = jsonWriter.getBytes();
        assertEquals("\"2023-03-26 10:25:19\"", JSONB.toJSONString(bytes));
        LocalDateTime ldt1 = JSONB.parseObject(bytes, LocalDateTime.class);
        assertEquals(ldt, ldt1);
    }

    @Test
    public void writeDate8() {
        LocalDate localDate = LocalDate.of(2023, 3, 26);
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeDateYYYMMDD8(
                localDate.getYear(),
                localDate.getMonthValue(),
                localDate.getDayOfMonth()
        );

        byte[] bytes = jsonWriter.getBytes();
        assertEquals("\"2023-03-26\"", JSONB.toJSONString(bytes));
        LocalDate localDate1 = JSONB.parseObject(bytes, LocalDate.class);
        assertEquals(localDate, localDate1);
    }

    public static class Bean {
        @JSONField(format = "yyyyMMddHHmmss")
        public Instant date;
    }

    @Test
    public void writeHex() {
        byte[] bytes = new byte[]{1, 2, 3};
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeHex(bytes);
        byte[] jsonbBytes = jsonWriter.getBytes();
        assertArrayEquals(bytes, (byte[]) JSONB.parse(jsonbBytes));

        assertThrows(Exception.class, () -> jsonWriter.getBytes(StandardCharsets.UTF_8));
    }
}
