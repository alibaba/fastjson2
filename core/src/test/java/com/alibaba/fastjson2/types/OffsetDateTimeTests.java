package com.alibaba.fastjson2.types;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriters;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

public class OffsetDateTimeTests {
    @Test
    public void test() throws Exception {
        OffsetDateTime dateTime = OffsetDateTime.of(
                LocalDateTime.of(2001, 2, 3, 12, 13, 14),
                ZoneOffset.ofHours(8));
        ObjectWriter objectWriter = ObjectWriters.objectWriter(
                ObjectWriterCreator.INSTANCE.createFieldWriter("value", OffsetDateTime.class, Bean::getValue)
        );

        String str = "{\"value\":\"" + dateTime + "\"}";

        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean bean = new Bean();
            bean.value = dateTime;
            objectWriter.write(jsonWriter, bean);
            assertEquals(str, jsonWriter.toString());

            assertNotNull(objectWriter.getFieldWriter("value").getFunction());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofJSONB(JSONWriter.Feature.WriteNulls);
            Bean bean = new Bean();
            bean.value = dateTime;
            objectWriter.write(jsonWriter, bean);
            Bean bean1 = JSONB.parseObject(jsonWriter.getBytes(), Bean.class);
            assertEquals(bean.value, bean1.value);
        }
        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean bean = new Bean();
            objectWriter.write(jsonWriter, bean);
            assertEquals("{}", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteNulls);
            Bean bean = new Bean();
            objectWriter.write(jsonWriter, bean);
            assertEquals("{\"value\":null}", jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean bean = new Bean();
            bean.value = dateTime;
            objectWriter.getFieldWriter("value").writeValue(jsonWriter, bean);
            assertEquals("\"" + dateTime + "\"", jsonWriter.toString());
        }

        {
            ObjectReader<Bean> objectReader = ObjectReaders.objectReader(
                    Bean.class,
                    Bean::new,
                    ObjectReaderCreator.INSTANCE.createFieldReader("value", Bean.class.getMethod("setValue", OffsetDateTime.class))
            );

            Bean object = objectReader.readObject(JSONReader.of(str));
            assertEquals(dateTime, object.value);

            FieldReader fieldReader = objectReader.getFieldReader("value");
            assertNotNull(fieldReader.getObjectReader(JSONReader.of(str)));
            assertNotNull(fieldReader.getObjectReader(JSONFactory.createReadContext()));
        }

        assertSame(
                OffsetDateTime.class,
                JSONFactory.getDefaultObjectReaderProvider()
                        .getObjectReader(OffsetDateTime.class)
                        .getObjectClass());
    }

    @Test
    public void test0() {
        {
            JSONReader.Context context = JSONFactory.createReadContext();
            context.setZoneId(ZoneOffset.UTC);
            context.setDateFormat("yyyyMMdd");
            BeanX bean = JSON.parseObject("{\"value\":\"20010203\"}", BeanX.class, context);
            assertEquals(
                    OffsetDateTime.of(
                            LocalDate.of(2001, 2, 3),
                            LocalTime.of(0, 0, 0),
                            ZoneOffset.UTC),
                    bean.value);
        }
        {
            JSONReader.Context context = JSONFactory.createReadContext();
            context.setZoneId(ZoneOffset.UTC);
            BeanX bean = JSON.parseObject("{\"value\":2000}", BeanX.class, context);
            assertEquals(
                    OffsetDateTime.of(
                            LocalDate.of(1970, 1, 1),
                            LocalTime.of(0, 0, 2),
                            ZoneOffset.UTC),
                    bean.value);
        }
        {
            JSONReader.Context context = JSONFactory.createReadContext();
            context.setZoneId(ZoneOffset.UTC);
            context.setDateFormat("millis");
            BeanX bean = JSON.parseObject("{\"value\":1000}", BeanX.class, context);
            assertEquals(
                    OffsetDateTime.of(
                            LocalDate.of(1970, 1, 1),
                            LocalTime.of(0, 0, 1),
                            ZoneOffset.UTC),
                    bean.value);
        }
        {
            JSONReader.Context context = JSONFactory.createReadContext();
            context.setZoneId(ZoneOffset.UTC);
            context.setDateFormat("millis");
            BeanX bean = JSON.parseObject("{\"value\":\"1000\"}", BeanX.class, context);
            assertEquals(
                    OffsetDateTime.of(
                            LocalDate.of(1970, 1, 1),
                            LocalTime.of(0, 0, 1),
                            ZoneOffset.UTC),
                    bean.value);
        }
        {
            JSONReader.Context context = JSONFactory.createReadContext();
            context.setZoneId(ZoneOffset.UTC);
            context.setDateFormat("unixtime");
            BeanX bean = JSON.parseObject("{\"value\":12}", BeanX.class, context);
            assertEquals(
                    OffsetDateTime.of(
                            LocalDate.of(1970, 1, 1),
                            LocalTime.of(0, 0, 12),
                            ZoneOffset.UTC),
                    bean.value);
        }
        {
            JSONReader.Context context = JSONFactory.createReadContext();
            context.setZoneId(ZoneOffset.UTC);
            context.setDateFormat("unixtime");
            BeanX bean = JSON.parseObject("{\"value\":\"12\"}", BeanX.class, context);
            assertEquals(
                    OffsetDateTime.of(
                            LocalDate.of(1970, 1, 1),
                            LocalTime.of(0, 0, 12),
                            ZoneOffset.UTC),
                    bean.value);
        }
    }

    public static class Bean {
        private OffsetDateTime value;

        public OffsetDateTime getValue() {
            return value;
        }

        public void setValue(OffsetDateTime value) {
            this.value = value;
        }
    }

    public static class BeanX {
        private final OffsetDateTime value;

        public BeanX(OffsetDateTime value) {
            this.value = value;
        }
    }

    @Test
    public void test1() {
        JSONReader.Context context = JSONFactory.createReadContext();
        context.setZoneId(ZoneOffset.UTC);
        Bean1 bean = JSON.parseObject("{\"time\":\"20010203\"}", Bean1.class, context);
        assertEquals(
                OffsetDateTime.of(
                        LocalDate.of(2001, 2, 3),
                        LocalTime.of(0, 0, 0),
                        ZoneOffset.UTC),
                bean.time);
    }

    public static class Bean1 {
        private final OffsetDateTime time;

        public Bean1(@JSONField(format = "yyyyMMdd")OffsetDateTime time) {
            this.time = time;
        }
    }

    @Test
    public void test2() {
        JSONReader.Context context = JSONFactory.createReadContext();
        context.setZoneId(ZoneOffset.UTC);
        Bean2 bean = JSON.parseObject("{\"time\":\"121314\"}", Bean2.class, context);
        assertEquals(
                OffsetDateTime.of(
                        LocalDate.of(1970, 1, 1),
                        LocalTime.of(12, 13, 14),
                        ZoneOffset.UTC),
                bean.time);
    }

    public static class Bean2 {
        private final OffsetDateTime time;

        public Bean2(@JSONField(format = "HHmmss")OffsetDateTime time) {
            this.time = time;
        }
    }

    @Test
    public void test3() {
        JSONReader.Context context = JSONFactory.createReadContext();
        context.setZoneId(ZoneOffset.UTC);
        Bean3 bean = JSON.parseObject("{\"time\":\"20010203121314\"}", Bean3.class, context);
        assertEquals(
                OffsetDateTime.of(
                        LocalDate.of(2001, 2, 3),
                        LocalTime.of(12, 13, 14),
                        ZoneOffset.UTC),
                bean.time);
    }

    public static class Bean3 {
        private final OffsetDateTime time;

        public Bean3(@JSONField(format = "yyyyMMddHHmmss")OffsetDateTime time) {
            this.time = time;
        }
    }

    @Test
    public void test4() {
        JSONReader.Context context = JSONFactory.createReadContext();
        context.setZoneId(ZoneOffset.UTC);
        Bean4 bean = JSON.parseObject("{\"time\":\"1000\"}", Bean4.class, context);
        assertEquals(
                OffsetDateTime.of(
                        LocalDate.of(1970, 1, 1),
                        LocalTime.of(0, 0, 1),
                        ZoneOffset.UTC),
                bean.time);
    }

    public static class Bean4 {
        private final OffsetDateTime time;

        public Bean4(@JSONField(format = "millis")OffsetDateTime time) {
            this.time = time;
        }
    }

    @Test
    public void test5() {
        JSONReader.Context context = JSONFactory.createReadContext();
        context.setZoneId(ZoneOffset.UTC);
        Bean5 bean = JSON.parseObject("{\"time\":\"1\"}", Bean5.class, context);
        assertEquals(
                OffsetDateTime.of(
                        LocalDate.of(1970, 1, 1),
                        LocalTime.of(0, 0, 1),
                        ZoneOffset.UTC),
                bean.time);
    }

    public static class Bean5 {
        private final OffsetDateTime time;

        public Bean5(@JSONField(format = "unixtime")OffsetDateTime time) {
            this.time = time;
        }
    }
}
