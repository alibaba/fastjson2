package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.Calendar1;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Calendar1Test {
    private TimeZone defaultTimeZone;

    Calendar[] dates = new Calendar[]{
            null,
            calendar(0),
            calendar(1),
            calendar(10),
            calendar(100),
            calendar(1000),
            calendar(10000),
            calendar(100000),
            calendar(1000000),
            calendar(10000000),
            calendar(100000000),
            calendar(1000000000),
            calendar(10000000000L),
            calendar(100000000000L),
            calendar(1000000000000L),
            calendar(10000000000000L),
            calendar(100000000000000L),
            calendar(-1),
            calendar(-10),
            calendar(-100),
            calendar(-1000),
            calendar(-10000),
            calendar(-100000),
            calendar(-1000000),
            calendar(-10000000),
            calendar(-100000000),
            calendar(-1000000000),
            calendar(-10000000000L),
            calendar(-100000000000L),
            calendar(-1000000000000L),
            calendar(-10000000000000L),
            calendar(1637467712394L)
    };

    static Calendar calendar(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar;
    }

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
    public void test_jsonb_0() {
        assertEquals(11, JSONB.parseObject(
                JSONB.toBytes("20171213"),
                Calendar.class).get(Calendar.MONTH));
        assertEquals(1, JSONB.parseObject(
                JSONB.toBytes("2017-2-3"),
                Calendar.class).get(Calendar.MONTH));
        assertEquals(11, JSONB.parseObject(
                JSONB.toBytes("2017-12-13"),
                Calendar.class).get(Calendar.MONTH));
        assertEquals(11, JSONB.parseObject(
                JSONB.toBytes("2017-12-3"),
                Calendar.class).get(Calendar.MONTH));
        assertEquals(6, JSONB.parseObject(
                JSONB.toBytes("2017-7-13"),
                Calendar.class).get(Calendar.MONTH));
        assertEquals(11, JSONB.parseObject(
                JSONB.toBytes("2017/12/13"),
                Calendar.class).get(Calendar.MONTH));
        assertEquals(11, JSONB.parseObject(
                JSONB.toBytes("13.12.2017"),
                Calendar.class).get(Calendar.MONTH));
        assertEquals(11, JSONB.parseObject(
                JSONB.toBytes("13-12-2017"),
                Calendar.class).get(Calendar.MONTH));

        assertEquals(2017, JSONB.parseObject(
                JSONB.toBytes("2017-12-13 00:00:00"),
                Calendar.class).get(Calendar.YEAR));
        assertEquals(2017, JSONB.parseObject(
                JSONB.toBytes("2017/12/13 00:00:00"),
                Calendar.class).get(Calendar.YEAR));
    }

    @Test
    public void test_utf16_0() {
        assertEquals(6,
                JSON.parseObject(
                        "\"2017년7월3일\"",
                        Calendar.class
                ).get(Calendar.MONTH));
        assertEquals(6,
                JSON.parseObject(
                        "\"2017년7월13일\"",
                        Calendar.class
                ).get(Calendar.MONTH));

        assertEquals(6,
                JSON.parseObject(
                        "\"2017-7-13\"",
                        Calendar.class
                ).get(Calendar.MONTH));
        assertEquals(11,
                JSON.parseObject(
                        "\"2017-12-7\"",
                        Calendar.class
                ).get(Calendar.MONTH));
        assertEquals(11,
                JSON.parseObject(
                        "\"2017-12-17\"",
                        Calendar.class
                ).get(Calendar.MONTH));
        assertEquals(11,
                JSON.parseObject(
                        "\"2017年12月17日\"",
                        Calendar.class
                ).get(Calendar.MONTH));
    }

    @Test
    public void test_utf8_0() {
        assertEquals(2017,
                JSON.parseObject(
                        "\"20171213\""
                                .getBytes(StandardCharsets.UTF_8),
                        Calendar.class
                ).get(Calendar.YEAR));
        assertEquals(2017,
                JSON.parseObject(
                        "\"2017-2-3\""
                                .getBytes(StandardCharsets.UTF_8),
                        Calendar.class
                ).get(Calendar.YEAR));
        assertEquals(11,
                JSON.parseObject(
                        "\"2017-12-3\""
                                .getBytes(StandardCharsets.UTF_8),
                        Calendar.class
                ).get(Calendar.MONTH));
        assertEquals(2,
                JSON.parseObject(
                        "\"2017-3-13\""
                                .getBytes(StandardCharsets.UTF_8),
                        Calendar.class
                ).get(Calendar.MONTH));
        assertEquals(2017,
                JSON.parseObject(
                        "\"2017年1月13日\""
                                .getBytes(StandardCharsets.UTF_8),
                        Calendar.class
                ).get(Calendar.YEAR));
        assertEquals(11,
                JSON.parseObject(
                        "\"2017年12月13日\""
                                .getBytes(StandardCharsets.UTF_8),
                        Calendar.class
                ).get(Calendar.MONTH));
        assertEquals(2017,
                JSON.parseObject(
                        "\"2017年11月9日\""
                                .getBytes(StandardCharsets.UTF_8),
                        Calendar.class
                ).get(Calendar.YEAR));
    }

    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<Calendar1> objectWriter
                    = creator.createObjectWriter(Calendar1.class);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                Calendar1 vo = new Calendar1();
                vo.setDate(calendar(0));
                objectWriter.write(jsonWriter, vo);
                assertEquals("[\"1970-01-01 08:00:00\"]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                jsonWriter.getContext().setDateFormat("millis");
                Calendar1 vo = new Calendar1();
                vo.setDate(calendar(0));
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
            FieldWriter fieldWriter = creator.createFieldWriter(Calendar1.class, "date", 0, 0, null, Calendar1.class.getMethod("getDate"));
            ObjectWriter<Calendar1> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                Calendar1 vo = new Calendar1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                Calendar1 vo = new Calendar1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                Calendar1 vo = new Calendar1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"date\":null}",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_jsonb() {
        for (int i = 0; i < dates.length; ++i) {
            Calendar date = dates[i];

            Calendar1 vo = new Calendar1();
            vo.setDate(date);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            Calendar1 v1 = JSONB.parseObject(jsonbBytes, Calendar1.class);
            assertEquals(date, v1.getDate());
        }
    }

    @Test
    public void test_jsonb_value() {
        for (Calendar date : dates) {
            byte[] jsonbBytes = JSONB.toBytes(date);
            Calendar id2 = JSONB.parseObject(jsonbBytes, Calendar.class);
            assertEquals(date, id2);
        }
    }

    @Test
    public void test_jsonb_value_cast() {
        for (Calendar date : dates) {
            byte[] jsonbBytes = JSONB.toBytes(date);
            Long date2 = JSONB.parseObject(jsonbBytes, Long.class);
            if (date == null) {
                assertNull(date2);
                continue;
            }
            assertEquals(date.getTimeInMillis(), date2.longValue());
        }
    }

    @Test
    public void test_jsonb_value_cast1() {
        for (Calendar date : dates) {
            if (date == null) {
                continue;
            }

            byte[] jsonbBytes = JSONB.toBytes(date.getTimeInMillis());
            Calendar date2 = JSONB.parseObject(jsonbBytes, Calendar.class);
            if (date == null) {
                assertNull(date2);
                continue;
            }
            assertEquals(date.getTimeInMillis(), date2.getTimeInMillis());
        }
    }

    @Test
    public void test_jsonb_array() {
        byte[] jsonbBytes = JSONB.toBytes(dates);
        Calendar[] date2 = JSONB.parseObject(jsonbBytes, Calendar[].class);
        assertEquals(dates.length, date2.length);
        for (int i = 0; i < date2.length; ++i) {
            assertEquals(dates[i], date2[i]);
        }
    }

    @Test
    public void test_str() {
        for (Calendar id : dates) {
            Calendar1 vo = new Calendar1();
            vo.setDate(id);
            String str = JSON.toJSONString(vo);

            Calendar1 v1 = JSON.parseObject(str, Calendar1.class);
            assertEquals(vo.getDate(), v1.getDate());
        }
    }

    @Test
    public void test_str_value() {
        for (int i = 0; i < dates.length; i++) {
            Calendar id = dates[i];
            String str = JSON.toJSONString(id);
            Calendar id2 = JSON.parseObject(str, Calendar.class);
            assertEquals(id, id2, str);
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
            primitiveValues[i] = dates[i].getTimeInMillis();
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
            Calendar id = dates[i];

            Calendar1 vo = new Calendar1();
            vo.setDate(id);
            byte[] utf8 = JSON.toJSONBytes(vo);

            Calendar1 v1 = JSON.parseObject(utf8, Calendar1.class);
            assertEquals(vo.getDate(), v1.getDate());
        }
    }

    @Test
    public void test_utf8_value() {
        for (int i = 0; i < dates.length; i++) {
            Calendar id = dates[i];
            byte[] utf8 = JSON.toJSONBytes(id);
            Calendar id2 = JSON.parseObject(utf8, Calendar.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_utf8_value1() {
        for (int i = 0; i < dates.length; i++) {
            Calendar id = dates[i];
            if (id == null) {
                continue;
            }
            byte[] utf8 = JSON.toJSONBytes(new Date(id.getTimeInMillis()));
            Calendar id2 = JSON.parseObject(utf8, Calendar.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_utf8_value2() {
        for (int i = 0; i < dates.length; i++) {
            Calendar id = dates[i];
            if (id == null) {
                continue;
            }
            byte[] utf8 = JSON.toJSONBytes(id.getTimeInMillis());
            Calendar id2 = JSON.parseObject(utf8, Calendar.class);
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
            primitiveValues[i] = dates[i].getTimeInMillis();
        }
        byte[] utf8 = JSON.toJSONBytes(primitiveValues);
        Calendar[] id2 = JSON.parseObject(utf8, Calendar[].class);
        assertEquals(dates.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(primitiveValues[i], id2[i].getTimeInMillis());
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
            primitiveValues[i] = dates[i].getTimeInMillis();
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
            Calendar id = dates[i];

            Calendar1 vo = new Calendar1();
            vo.setDate(id);
            byte[] utf8 = JSON.toJSONBytes(vo);

            Calendar1 v1 = JSON.parseObject(utf8, 0, utf8.length, StandardCharsets.US_ASCII, Calendar1.class);
            if (id == null) {
                assertNull(v1.getDate());
                continue;
            }
            assertEquals(vo.getDate().getTime(), v1.getDate().getTime());
        }
    }

    @Test
    public void test_ascii_value() {
        for (int i = 0; i < dates.length; i++) {
            Calendar id = dates[i];
            byte[] utf8 = JSON.toJSONBytes(id);
            Calendar id2 = JSON.parseObject(utf8, 0, utf8.length, StandardCharsets.US_ASCII, Calendar.class);
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
            primitiveValues[i] = dates[i].getTimeInMillis();
        }
        byte[] utf8 = JSON.toJSONBytes(primitiveValues);
        long[] id2 = JSON.parseObject(utf8, 0, utf8.length, StandardCharsets.US_ASCII, long[].class);
        assertEquals(dates.length, id2.length);
        for (int i = 0; i < id2.length; ++i) {
            assertEquals(primitiveValues[i], id2[i]);
        }
    }
}
