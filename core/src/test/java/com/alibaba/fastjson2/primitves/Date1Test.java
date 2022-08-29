package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.Date1;
import com.alibaba.fastjson2_vo.LongValue1;
import com.alibaba.fastjson2_vo.LongValueField1;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

public class Date1Test {
    private TimeZone defaultTimeZone;

    Date[] dates = new Date[]{
            null,
            new Date(0),
            new Date(1),
            new Date(10),
            new Date(100),
            new Date(1000),
            new Date(10000),
            new Date(100000),
            new Date(1000000),
            new Date(10000000),
            new Date(100000000),
            new Date(1000000000),
            new Date(10000000000L),
            new Date(100000000000L),
            new Date(1000000000000L),
            new Date(10000000000000L),
            new Date(100000000000000L),
            new Date(-1),
            new Date(-10),
            new Date(-100),
            new Date(-1000),
            new Date(-10000),
            new Date(-100000),
            new Date(-1000000),
            new Date(-10000000),
            new Date(-100000000),
            new Date(-1000000000),
            new Date(-10000000000L),
            new Date(-100000000000L),
            new Date(-1000000000000L),
            new Date(-10000000000000L),
            new Date(1637467712394L)
    };

    @BeforeEach
    public void before() {
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    @AfterEach
    public void after() {
        TimeZone.setDefault(defaultTimeZone);
    }

    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<Date1> objectWriter
                    = creator.createObjectWriter(Date1.class);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                Date1 vo = new Date1();
                vo.setDate(new Date(0));
                objectWriter.write(jsonWriter, vo);
                assertEquals("[\"1970-01-01 08:00:00\"]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                jsonWriter.getContext().setDateFormat("millis");
                Date1 vo = new Date1();
                vo.setDate(new Date(0));
                objectWriter.write(jsonWriter, vo);
                assertEquals("[0]",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_null() throws Exception {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            FieldWriter fieldWriter = creator.createFieldWriter(Date1.class, "date", 0, 0, null, Date1.class.getMethod("getDate"));
            ObjectWriter<Date1> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                Date1 vo = new Date1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                Date1 vo = new Date1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                Date1 vo = new Date1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"date\":null}",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_millis() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            FieldWriter fieldWriter = creator.createFieldWriter("date", 0, "millis", Date.class, Date1::getDate);
            ObjectWriter<Date1> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                Date1 vo = new Date1();
                vo.setDate(new Date(0));
                objectWriter.write(jsonWriter, vo);
                assertEquals("[0]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                Date1 vo = new Date1();
                vo.setDate(new Date(0));
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"date\":0}",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_millis2() throws Exception {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            FieldWriter fieldWriter = creator
                    .createFieldWriter(LongValue1.class,
                            "date",
                            0,
                            0,
                            "iso8601",
                            LongValue1.class.getMethod("getV0000"));
            ObjectWriter<LongValue1> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                LongValue1 vo = new LongValue1();
                vo.setV0000(0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[\"1970-01-01 08:00:00\"]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                LongValue1 vo = new LongValue1();
                vo.setV0000(0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"date\":\"1970-01-01T08:00:00+08:00\"}",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_millis3() throws Exception {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            FieldWriter fieldWriter = creator
                    .createFieldWriter(
                            "date",
                            "iso8601",
                            LongValueField1.class.getField("v0000"));
            ObjectWriter<LongValueField1> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                LongValueField1 vo = new LongValueField1();
                vo.v0000 = 0;
                objectWriter.write(jsonWriter, vo);
                assertEquals("[\"1970-01-01 08:00:00\"]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                LongValueField1 vo = new LongValueField1();
                vo.v0000 = 0;
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"date\":\"1970-01-01T08:00:00+08:00\"}",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_jsonb() {
        for (int i = 0; i < dates.length; ++i) {
            Date date = dates[i];

            Date1 vo = new Date1();
            vo.setDate(date);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            Date1 v1 = JSONB.parseObject(jsonbBytes, Date1.class);
            assertEquals(date, v1.getDate());
        }
    }

    @Test
    public void test_jsonb_value() {
        for (int i = 0; i < dates.length; i++) {
            Date date = dates[i];

            byte[] jsonbBytes = JSONB.toBytes(date);
            Date id2 = JSONB.parseObject(jsonbBytes, Date.class);
            assertEquals(date, id2);
        }
    }

    @Test
    public void test_jsonb_value_cast() {
        for (Date date : dates) {
            byte[] jsonbBytes = JSONB.toBytes(date);
            Long date2 = JSONB.parseObject(jsonbBytes, Long.class);
            if (date == null) {
                assertNull(date2);
                continue;
            }
            assertEquals(date.getTime(), date2.longValue());
        }
    }

    @Test
    public void test_jsonb_value_cast1() {
        for (Date date : dates) {
            if (date == null) {
                continue;
            }

            byte[] jsonbBytes = JSONB.toBytes(date.getTime());
            Date date2 = JSONB.parseObject(jsonbBytes, Date.class);
            if (date == null) {
                assertNull(date2);
                continue;
            }
            assertEquals(date.getTime(), date2.getTime());
        }
    }

    @Test
    public void test_jsonb_value_cast2() {
        for (Date date : dates) {
            if (date == null) {
                continue;
            }

            byte[] jsonbBytes = JSONB.toBytes(Long.valueOf(date.getTime()));
            Date date2 = JSONB.parseObject(jsonbBytes, Date.class);
            if (date == null) {
                assertNull(date2);
                continue;
            }
            assertEquals(date.getTime(), date2.getTime());
        }
    }

    @Test
    public void test_jsonb_array() {
        byte[] jsonbBytes = JSONB.toBytes(dates);
        Date[] date2 = JSONB.parseObject(jsonbBytes, Date[].class);
        assertEquals(dates.length, date2.length);
        for (int i = 0; i < date2.length; ++i) {
            assertEquals(dates[i], date2[i]);
        }
    }

    @Test
    public void test_str() {
        for (Date id : dates) {
            Date1 vo = new Date1();
            vo.setDate(id);
            String str = JSON.toJSONString(vo);

            Date1 v1 = JSON.parseObject(str, Date1.class);
            assertEquals(vo.getDate(), v1.getDate());
        }
    }

    @Test
    public void test_str_value() {
        for (int i = 0; i < dates.length; i++) {
            Date date0 = dates[i];
            String str = JSON.toJSONString(date0);
            Date date1 = JSON.parseObject(str, Date.class);
            if (date0 == null) {
                assertNull(date1);
                continue;
            }
            assertEquals(date0.getTime(), date1.getTime(), str);
        }
    }

    @Test
    public void test_str_value_utc() {
        ZoneId utc = ZoneOffset.UTC;
        for (int i = 0; i < dates.length; i++) {
            Date date0 = dates[i];
            JSONWriter writer = JSONWriter.of();
            writer.getContext().setZoneId(utc);
            writer.writeAny(date0);
            String str = writer.toString();

            JSONReader reader = JSONReader.of(str);
            reader.getContext().setZoneId(utc);
            Date date1 = reader.read(Date.class);
            if (date0 == null) {
                assertNull(date1);
                continue;
            }
            assertEquals(date0.getTime(), date1.getTime(), str);
        }
    }

    @Test
    public void test_str_value_zone() {
        ZoneId[] zoneIds = {
                ZoneId.of("Asia/Macau"),
                ZoneOffset.UTC,
                ZoneId.of("Asia/Kuching"),
                ZoneId.of("Europe/London")
        };
        for (ZoneId zoneId : zoneIds) {
            for (int i = 0; i < dates.length; i++) {
                Date date0 = dates[i];
                JSONWriter writer = JSONWriter.of();
                writer.getContext().setZoneId(zoneId);
                writer.writeAny(date0);
                String str = writer.toString();

                JSONReader reader = JSONReader.of(str);
                reader.getContext().setZoneId(zoneId);
                Date date1 = reader.read(Date.class);
                if (date0 == null) {
                    assertNull(date1);
                    continue;
                }
                assertEquals(date0.getTime(), date1.getTime(), str);
            }
        }
    }

    @Test
    public void test_str_array3() {
        long[] primitiveValues = new long[dates.length];
        for (int i = 0; i < dates.length; i++) {
            if (dates[i] == null) {
                primitiveValues[i] = 0;
                continue;
            }
            primitiveValues[i] = dates[i].getTime();
        }
        String str = JSON.toJSONString(primitiveValues);
        long[] id2 = JSON.parseObject(str, long[].class);
        assertEquals(dates.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(primitiveValues[i], id2[i]);
        }
    }

    @Test
    public void test_utf8() {
        for (int i = 0; i < dates.length; i++) {
            Date id = dates[i];

            Date1 vo = new Date1();
            vo.setDate(id);
            byte[] utf8 = JSON.toJSONBytes(vo);

            Date1 v1 = JSON.parseObject(utf8, Date1.class);
            assertEquals(vo.getDate(), v1.getDate());
        }
    }

    @Test
    public void test_utf8_value() {
        for (int i = 0; i < dates.length; i++) {
            Date id = dates[i];
            byte[] utf8 = JSON.toJSONBytes(id);
            Date id2 = JSON.parseObject(utf8, Date.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_utf8_value2() {
        for (int i = 0; i < dates.length; i++) {
            Date id = dates[i];
            if (id == null) {
                continue;
            }
            byte[] utf8 = JSON.toJSONBytes(id.getTime());
            Date id2 = JSON.parseObject(utf8, Date.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_utf8_array2() {
        long[] primitiveValues = new long[dates.length];
        for (int i = 0; i < dates.length; i++) {
            if (dates[i] == null) {
                primitiveValues[i] = 0;
                continue;
            }
            primitiveValues[i] = dates[i].getTime();
        }
        byte[] utf8 = JSON.toJSONBytes(primitiveValues);
        Date[] id2 = JSON.parseObject(utf8, Date[].class);
        assertEquals(dates.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(primitiveValues[i], id2[i].getTime());
        }
    }

    @Test
    public void test_utf8_array3() {
        long[] primitiveValues = new long[dates.length];
        for (int i = 0; i < dates.length; i++) {
            if (dates[i] == null) {
                primitiveValues[i] = 0;
                continue;
            }
            primitiveValues[i] = dates[i].getTime();
        }
        byte[] utf8 = JSON.toJSONBytes(primitiveValues);
        long[] id2 = JSON.parseObject(utf8, long[].class);
        assertEquals(dates.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(primitiveValues[i], id2[i]);
        }
    }

    @Test
    public void test_ascii() {
        for (int i = 0; i < dates.length; i++) {
            Date id = dates[i];

            Date1 vo = new Date1();
            vo.setDate(id);
            byte[] utf8 = JSON.toJSONBytes(vo);

            Date1 v1 = JSON.parseObject(utf8, 0, utf8.length, StandardCharsets.US_ASCII, Date1.class);
            assertEquals(vo.getDate(), v1.getDate());
        }
    }

    @Test
    public void test_ascii_value() {
        for (int i = 0; i < dates.length; i++) {
            Date id = dates[i];
            byte[] utf8 = JSON.toJSONBytes(id);
            Date id2 = JSON.parseObject(utf8, 0, utf8.length, StandardCharsets.US_ASCII, Date.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_ascii_array3() {
        long[] primitiveValues = new long[dates.length];
        for (int i = 0; i < dates.length; i++) {
            if (dates[i] == null) {
                primitiveValues[i] = 0;
                continue;
            }
            primitiveValues[i] = dates[i].getTime();
        }
        byte[] utf8 = JSON.toJSONBytes(primitiveValues);
        long[] id2 = JSON.parseObject(utf8, 0, utf8.length, StandardCharsets.US_ASCII, long[].class);
        assertEquals(dates.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(primitiveValues[i], id2[i]);
        }
    }

    @Test
    public void test_byteBuffer() {
        for (int i = 0; i < dates.length; i++) {
            Date id = dates[i];

            Date1 vo = new Date1();
            vo.setDate(id);
            byte[] utf8 = JSON.toJSONBytes(vo);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(utf8);

            JSONReader jsonReader = JSONReader.of(byteIn, StandardCharsets.UTF_8);
            Date1 v1 = jsonReader.read(Date1.class);
            assertEquals(vo.getDate(), v1.getDate());
        }
    }

    @Test
    public void test1() throws Exception {
        String str = "1899-01-01T08:00:00";
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
//        LocalDateTime ldt = LocalDateTime.parse(str);
        LocalDateTime ldt = LocalDateTime.of(1899, 1, 1, 8, 0, 0);
        ZoneRules zoneIdRules = zoneId.getRules();
        ZoneOffset zoneOffset = zoneIdRules.getOffset(ldt);
        int zoneOffsetTotalSeconds = zoneOffset.getTotalSeconds();
        long epochMilli = ldt.atZone(zoneId).toInstant().toEpochMilli();

        zoneIdRules.getOffset(Instant.ofEpochMilli(epochMilli));

        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        format.setTimeZone(timeZone);

        TimeZone timeZone8 = TimeZone.getTimeZone("GMT+08:00");
        SimpleDateFormat format8 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        format.setTimeZone(timeZone8);

        Date date1 = format.parse(str);
        Date date2 = format8.parse(str);

        int rawOffset = timeZone.getRawOffset(); // 28800000  +8:00:00
        int timeZoneOffset = timeZone.getOffset(date1.getTime()); // 28800000 +8:00:00
        int zoneOffsetTotalMillis = zoneOffsetTotalSeconds * 1000; // 29143000 +8:05:43

        Date date0 = new Date(epochMilli);
        System.out.println(date1);
        System.out.println(date2);
    }
}
