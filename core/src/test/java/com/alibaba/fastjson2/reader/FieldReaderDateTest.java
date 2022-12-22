package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderDateTest {
    @Test
    public void test() throws Exception {
        Field field = Bean.class.getDeclaredField("date");
        FieldReaderDate fieldReader = new FieldReaderDate(field.getName(), field.getType(), field.getType(), 0, 0, null, null, null, null, field, null, null);

        Instant instant = Instant.now();

        assertThrows(Exception.class, () -> fieldReader.accept(null, instant));

        Bean bean = new Bean();
        fieldReader.accept(bean, instant);

        assertNotNull(bean.date);
        long epochMilli = instant.toEpochMilli();
        assertEquals(epochMilli, bean.date.getTime());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, epochMilli);
        assertEquals(epochMilli, bean.date.getTime());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        ZonedDateTime zdt = instant.atZone(DateUtils.DEFAULT_ZONE_ID);
        fieldReader.accept(bean, zdt);
        assertEquals(epochMilli, bean.date.getTime());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, zdt.toLocalDateTime());
        assertEquals(epochMilli, bean.date.getTime());

        assertSame(ObjectReaderImplDate.INSTANCE, fieldReader.getObjectReader(JSONFactory.createReadContext()));

        assertEquals(bean.date, fieldReader.apply(instant));
        assertEquals(bean.date, fieldReader.apply(epochMilli));
        assertEquals(bean.date, fieldReader.apply(zdt));
        assertEquals(bean.date, fieldReader.apply(zdt.toLocalDateTime()));
        assertEquals(bean.date, fieldReader.apply(new Date(epochMilli)));
    }

    @Test
    public void testReflect() throws Exception {
        Field field = Bean.class.getDeclaredField("date");
        FieldReaderDate fieldReader = new FieldReaderDate(field.getName(), field.getType(), field.getType(), 0, FieldInfo.DISABLE_UNSAFE, null, null, null, null, field, null, null);

        Instant instant = Instant.now();

        assertThrows(Exception.class, () -> fieldReader.accept(null, instant));

        Bean bean = new Bean();
        fieldReader.accept(bean, instant);

        assertNotNull(bean.date);
        long epochMilli = instant.toEpochMilli();
        assertEquals(epochMilli, bean.date.getTime());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, epochMilli);
        assertEquals(epochMilli, bean.date.getTime());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        ZonedDateTime zdt = instant.atZone(DateUtils.DEFAULT_ZONE_ID);
        fieldReader.accept(bean, zdt);
        assertEquals(epochMilli, bean.date.getTime());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, zdt.toLocalDateTime());
        assertEquals(epochMilli, bean.date.getTime());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, new Date(epochMilli));
        assertEquals(epochMilli, bean.date.getTime());

        assertSame(ObjectReaderImplDate.INSTANCE, fieldReader.getObjectReader(JSONFactory.createReadContext()));

        assertEquals(bean.date, fieldReader.apply(instant));
        assertEquals(bean.date, fieldReader.apply(epochMilli));
        assertEquals(bean.date, fieldReader.apply(zdt));
        assertEquals(bean.date, fieldReader.apply(zdt.toLocalDateTime()));
        assertEquals(bean.date, fieldReader.apply(new Date(epochMilli)));
    }

    public static class Bean {
        private Date date;
    }

    @Test
    public void test1() throws Exception {
        Field field = Bean1.class.getDeclaredField("date");
        FieldReaderLocalDateTime fieldReader = new FieldReaderLocalDateTime<>(field.getName(), field.getType(), field.getType(), 0, 0, null, null, null, null, field, null, null);

        long epochMilli = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(epochMilli);

        assertThrows(Exception.class, () -> fieldReader.accept(null, instant));

        Bean1 bean = new Bean1();
        fieldReader.accept(bean, instant);

        assertNotNull(bean.date);
        ZoneId zoneId = DateUtils.DEFAULT_ZONE_ID;
        assertEquals(epochMilli, bean.toMillis(zoneId));

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, epochMilli);
        assertEquals(epochMilli, bean.toMillis(zoneId));

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        ZonedDateTime zdt = instant.atZone(zoneId);
        fieldReader.accept(bean, zdt);
        assertEquals(epochMilli, bean.toMillis(zoneId));

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, zdt.toLocalDateTime());
        assertEquals(epochMilli, bean.toMillis(zoneId));

        assertSame(ObjectReaderImplLocalDateTime.INSTANCE, fieldReader.getObjectReader(JSONFactory.createReadContext()));

        assertEquals(bean.date, fieldReader.apply(instant));
        assertEquals(bean.date, fieldReader.apply(epochMilli));
        assertEquals(bean.date, fieldReader.apply(zdt));
        assertEquals(bean.date, fieldReader.apply(zdt.toLocalDateTime()));
        assertEquals(bean.date, fieldReader.apply(new Date(epochMilli)));
    }

    @Test
    public void test1Reflect() throws Exception {
        Field field = Bean1.class.getDeclaredField("date");
        FieldReaderLocalDateTime fieldReader = new FieldReaderLocalDateTime<>(field.getName(), field.getType(), field.getType(), 0, FieldInfo.DISABLE_UNSAFE, null, null, null, null, field, null, null);

        long epochMilli = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(epochMilli);

        assertThrows(Exception.class, () -> fieldReader.accept(null, instant));

        Bean1 bean = new Bean1();
        fieldReader.accept(bean, instant);

        assertNotNull(bean.date);
        ZoneId zoneId = DateUtils.DEFAULT_ZONE_ID;
        assertEquals(epochMilli, bean.toMillis(zoneId));

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, epochMilli);
        assertEquals(epochMilli, bean.toMillis(zoneId));

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        ZonedDateTime zdt = instant.atZone(zoneId);
        fieldReader.accept(bean, zdt);
        assertEquals(epochMilli, bean.toMillis(zoneId));

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, zdt.toLocalDateTime());
        assertEquals(epochMilli, bean.toMillis(zoneId));

        fieldReader.acceptNull(bean);
        assertNull(bean.date);
        fieldReader.accept(bean, new Date(epochMilli));
        assertEquals(epochMilli, bean.toMillis(zoneId));

        assertSame(ObjectReaderImplLocalDateTime.INSTANCE, fieldReader.getObjectReader(JSONFactory.createReadContext()));

        assertEquals(bean.date, fieldReader.apply(instant));
        assertEquals(bean.date, fieldReader.apply(epochMilli));
        assertEquals(bean.date, fieldReader.apply(zdt));
        assertEquals(bean.date, fieldReader.apply(zdt.toLocalDateTime()));
        assertEquals(bean.date, fieldReader.apply(new Date(epochMilli)));

        fieldReader.field.setAccessible(false);
        assertThrows(Exception.class, () -> fieldReader.accept(bean, epochMilli));
    }

    public static class Bean1 {
        private LocalDateTime date;

        public long toMillis(ZoneId zoneId) {
            return date.toInstant(zoneId.getRules().getOffset(date)).toEpochMilli();
        }
    }

    @Test
    public void test2() throws Exception {
        Field field = Bean2.class.getDeclaredField("date");
        FieldReaderInstant fieldReader = new FieldReaderInstant<>(field.getName(), field.getType(), field.getType(), 0, 0, null, null, null, null, field, null, null);

        long epochMilli = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(epochMilli);

        assertThrows(Exception.class, () -> fieldReader.accept(null, instant));

        Bean2 bean = new Bean2();
        fieldReader.accept(bean, instant);

        assertNotNull(bean.date);
        ZoneId zoneId = DateUtils.DEFAULT_ZONE_ID;
        assertEquals(epochMilli, bean.date.toEpochMilli());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, epochMilli);
        assertEquals(epochMilli, bean.date.toEpochMilli());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        ZonedDateTime zdt = instant.atZone(zoneId);
        fieldReader.accept(bean, zdt);
        assertEquals(epochMilli, bean.date.toEpochMilli());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, zdt.toLocalDateTime());
        assertEquals(epochMilli, bean.date.toEpochMilli());

        assertSame(ObjectReaderImplInstant.INSTANCE, fieldReader.getObjectReader(JSONFactory.createReadContext()));

        assertEquals(bean.date, fieldReader.apply(instant));
        assertEquals(bean.date, fieldReader.apply(epochMilli));
        assertEquals(bean.date, fieldReader.apply(zdt));
        assertEquals(bean.date, fieldReader.apply(zdt.toLocalDateTime()));
        assertEquals(bean.date, fieldReader.apply(new Date(epochMilli)));
    }

    @Test
    public void test2Reflect() throws Exception {
        Field field = Bean2.class.getDeclaredField("date");
        FieldReaderInstant fieldReader = new FieldReaderInstant<>(field.getName(), field.getType(), field.getType(), 0, FieldInfo.DISABLE_UNSAFE, null, null, null, null, field, null, null);

        long epochMilli = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(epochMilli);

        assertThrows(Exception.class, () -> fieldReader.accept(null, instant));

        Bean2 bean = new Bean2();
        fieldReader.accept(bean, instant);

        assertNotNull(bean.date);
        ZoneId zoneId = DateUtils.DEFAULT_ZONE_ID;
        assertEquals(epochMilli, bean.date.toEpochMilli());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, epochMilli);
        assertEquals(epochMilli, bean.date.toEpochMilli());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);
        ZonedDateTime zdt = instant.atZone(zoneId);
        fieldReader.accept(bean, zdt);
        assertEquals(epochMilli, bean.date.toEpochMilli());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);
        fieldReader.accept(bean, zdt.toLocalDateTime());
        assertEquals(epochMilli, bean.date.toEpochMilli());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);
        fieldReader.accept(bean, new Date(epochMilli));
        assertEquals(epochMilli, bean.date.toEpochMilli());

        assertSame(ObjectReaderImplInstant.INSTANCE, fieldReader.getObjectReader(JSONFactory.createReadContext()));

        assertEquals(bean.date, fieldReader.apply(instant));
        assertEquals(bean.date, fieldReader.apply(epochMilli));
        assertEquals(bean.date, fieldReader.apply(zdt));
        assertEquals(bean.date, fieldReader.apply(zdt.toLocalDateTime()));
        assertEquals(bean.date, fieldReader.apply(new Date(epochMilli)));

        fieldReader.field.setAccessible(false);
        assertThrows(Exception.class, () -> fieldReader.accept(bean, epochMilli));
    }

    public static class Bean2 {
        private Instant date;
    }

    @Test
    public void test3() throws Exception {
        Field field = Bean3.class.getDeclaredField("date");
        FieldReaderZonedDateTime fieldReader = new FieldReaderZonedDateTime<>(field.getName(), field.getType(), field.getType(), 0, 0, null, null, null, null, field, null, null);

        long epochMilli = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(epochMilli);

        assertThrows(Exception.class, () -> fieldReader.accept(null, instant));

        Bean3 bean = new Bean3();
        fieldReader.accept(bean, instant);

        assertNotNull(bean.date);
        ZoneId zoneId = DateUtils.DEFAULT_ZONE_ID;
        assertEquals(epochMilli, bean.date.toInstant().toEpochMilli());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, epochMilli);
        assertEquals(epochMilli, bean.date.toInstant().toEpochMilli());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        ZonedDateTime zdt = instant.atZone(zoneId);
        fieldReader.accept(bean, zdt);
        assertEquals(epochMilli, bean.date.toInstant().toEpochMilli());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, zdt.toLocalDateTime());
        assertEquals(epochMilli, bean.date.toInstant().toEpochMilli());
        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, new Date(epochMilli));
        assertEquals(epochMilli, bean.date.toInstant().toEpochMilli());

        assertSame(ObjectReaderImplZonedDateTime.INSTANCE, fieldReader.getObjectReader(JSONFactory.createReadContext()));

        assertEquals(bean.date, fieldReader.apply(instant));
        assertEquals(bean.date, fieldReader.apply(epochMilli));
        assertEquals(bean.date, fieldReader.apply(zdt));
        assertEquals(bean.date, fieldReader.apply(zdt.toLocalDateTime()));
        assertEquals(bean.date, fieldReader.apply(new Date(epochMilli)));
    }

    @Test
    public void test3Reflect() throws Exception {
        Field field = Bean3.class.getDeclaredField("date");
        FieldReaderZonedDateTime fieldReader = new FieldReaderZonedDateTime<>(field.getName(), field.getType(), field.getType(), 0, FieldInfo.DISABLE_UNSAFE, null, null, null, null, field, null, null);

        long epochMilli = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(epochMilli);

        assertThrows(Exception.class, () -> fieldReader.accept(null, instant));

        Bean3 bean = new Bean3();
        fieldReader.accept(bean, instant);

        assertNotNull(bean.date);
        ZoneId zoneId = DateUtils.DEFAULT_ZONE_ID;
        assertEquals(epochMilli, bean.date.toInstant().toEpochMilli());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, epochMilli);
        assertEquals(epochMilli, bean.date.toInstant().toEpochMilli());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        ZonedDateTime zdt = instant.atZone(zoneId);
        fieldReader.accept(bean, zdt);
        assertEquals(epochMilli, bean.date.toInstant().toEpochMilli());

        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, zdt.toLocalDateTime());
        assertEquals(epochMilli, bean.date.toInstant().toEpochMilli());
        fieldReader.acceptNull(bean);
        assertNull(bean.date);

        fieldReader.accept(bean, new Date(epochMilli));
        assertEquals(epochMilli, bean.date.toInstant().toEpochMilli());

        assertSame(ObjectReaderImplZonedDateTime.INSTANCE, fieldReader.getObjectReader(JSONFactory.createReadContext()));

        assertEquals(bean.date, fieldReader.apply(instant));
        assertEquals(bean.date, fieldReader.apply(epochMilli));
        assertEquals(bean.date, fieldReader.apply(zdt));
        assertEquals(bean.date, fieldReader.apply(zdt.toLocalDateTime()));
        assertEquals(bean.date, fieldReader.apply(new Date(epochMilli)));

        fieldReader.field.setAccessible(false);
        assertThrows(Exception.class, () -> fieldReader.accept(bean, epochMilli));
    }

    public static class Bean3 {
        private ZonedDateTime date;
    }
}
