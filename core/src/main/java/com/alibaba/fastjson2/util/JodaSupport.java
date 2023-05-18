package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.support.LambdaMiscCodec;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.*;

import static com.alibaba.fastjson2.JSONB.Constants.BC_LOCAL_DATE;
import static com.alibaba.fastjson2.JSONB.Constants.BC_LOCAL_DATETIME;

public class JodaSupport {
    static final long HASH_YEAR = Fnv.hashCode64("year");
    static final long HASH_MONTH = Fnv.hashCode64("month");
    static final long HASH_DAY = Fnv.hashCode64("day");
    static final long HASH_HOUR = Fnv.hashCode64("hour");
    static final long HASH_MINUTE = Fnv.hashCode64("minute");
    static final long HASH_SECOND = Fnv.hashCode64("second");
    static final long HASH_MILLIS = Fnv.hashCode64("millis");
    static final long HASH_CHRONOLOGY = Fnv.hashCode64("chronology");

    public static ObjectWriter createLocalDateTimeWriter(Class objectClass, String format) {
        return new LocalDateTimeWriter(objectClass, format);
    }

    public static ObjectWriter createLocalDateWriter(Class objectClass, String format) {
        return new LocalDateWriter(objectClass, format);
    }

    public static ObjectReader createChronologyReader(Class objectClass) {
        return new ChronologyReader(objectClass);
    }

    public static ObjectReader createLocalDateReader(Class objectClass) {
        return new LocalDateReader(objectClass);
    }

    public static ObjectReader createLocalDateTimeReader(Class objectClass) {
        return new LocalDateTimeReader(objectClass);
    }

    public static ObjectReader createInstantReader(Class objectClass) {
        return new InstantReader(objectClass);
    }

    public static ObjectWriter createGregorianChronologyWriter(Class objectClass) {
        return new GregorianChronologyWriter(objectClass);
    }

    public static ObjectWriter createISOChronologyWriter(Class objectClass) {
        return new ISOChronologyWriter(objectClass);
    }

    static class InstantReader
            implements ObjectReader {
        final Class objectClass;
        final LongFunction constructor;

        InstantReader(Class objectClass) {
            this.objectClass = objectClass;
            try {
                this.constructor = LambdaMiscCodec.createLongFunction(
                        objectClass.getConstructor(long.class)
                );
            } catch (NoSuchMethodException e) {
                throw new JSONException("create joda instant reader error", e);
            }
        }

        @Override
        public Class getObjectClass() {
            return objectClass;
        }

        @Override
        public Object createInstance(Map map, long features) {
            Number millis = (Long) map.get("millis");
            if (millis != null) {
                return createInstanceFromMillis(millis.longValue());
            }

            Number epochSecond = (Number) map.get("epochSecond");
            if (epochSecond != null) {
                long epochMillis = epochSecond.longValue() * 1000L;
                return createInstanceFromMillis(epochMillis);
            }

            throw new JSONException("create joda instant error");
        }

        public Object createInstanceFromMillis(long millis) {
            return constructor.apply(millis);
        }

        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            if (jsonReader.nextIfNull()) {
                return null;
            }

            if (jsonReader.isInt()) {
                long millis = jsonReader.readInt64Value();
                return createInstanceFromMillis(millis);
            }

            if (jsonReader.isString()) {
                Instant jdkInstant = jsonReader.readInstant();
                if (jdkInstant == null) {
                    return null;
                }

                long millis = jdkInstant.toEpochMilli();
                return createInstanceFromMillis(millis);
            }

            if (jsonReader.isObject()) {
                Map object = jsonReader.readObject();
                return createInstance(object, features);
            }

            throw new JSONException(jsonReader.info("not support"));
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            return readObject(jsonReader, fieldType, fieldName, features);
        }
    }

    static class ChronologyReader
            implements ObjectReader {
        static final long HASH_ZONE_ID = Fnv.hashCode64("zoneId");

        final Class objectClass;
        final Class gregorianChronology;
        final Class dateTimeZone;
        final Function forID;
        final Function getInstance;
        final Object utc;

        ChronologyReader(Class objectClass) {
            this.objectClass = objectClass;
            ClassLoader classLoader = objectClass.getClassLoader();
            try {
                gregorianChronology = classLoader.loadClass("org.joda.time.chrono.GregorianChronology");
                dateTimeZone = classLoader.loadClass("org.joda.time.DateTimeZone");

                utc = gregorianChronology.getMethod("getInstanceUTC").invoke(null);
                forID = LambdaMiscCodec.createFunction(
                        dateTimeZone.getMethod("forID", String.class)
                );
                getInstance = LambdaMiscCodec.createFunction(
                        gregorianChronology.getMethod("getInstance", dateTimeZone)
                );
            } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new JSONException("create ChronologyReader error", e);
            }
        }

        @Override
        public Class getObjectClass() {
            return objectClass;
        }

        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            throw new JSONException(jsonReader.info("not support"));
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            Integer minimumDaysInFirstWeek = null;
            String zoneId = null;
            jsonReader.nextIfObjectStart();
            for (; ; ) {
                if (jsonReader.nextIfObjectEnd()) {
                    break;
                }
                final long HASH_MINIMUM_DAYS_IN_FIRST_WEEK = 8244232525129275563L; // Fnv.hashCode64("minimumDaysInFirstWeek");
                long fieldNameHashCode = jsonReader.readFieldNameHashCode();
                if (fieldNameHashCode == HASH_MINIMUM_DAYS_IN_FIRST_WEEK) {
                    minimumDaysInFirstWeek = jsonReader.readInt32Value();
                } else if (fieldNameHashCode == HASH_ZONE_ID) {
                    zoneId = jsonReader.readString();
                } else {
                    throw new JSONException(jsonReader.info("not support fieldName " + jsonReader.getFieldName()));
                }
            }

            if (minimumDaysInFirstWeek == null) {
                if ("UTC".equals(zoneId)) {
                    return utc;
                }

                Object datetimeZone = forID.apply(zoneId);
                return getInstance.apply(datetimeZone);
            }

            throw new JSONException(jsonReader.info("not support"));
        }
    }

    static class GregorianChronologyWriter
            implements ObjectWriter {
        final Class objectClass;
        final ToIntFunction getMinimumDaysInFirstWeek;
        final Function getZone;
        final Function getID;

        GregorianChronologyWriter(Class objectClass) {
            this.objectClass = objectClass;
            try {
                this.getMinimumDaysInFirstWeek = LambdaMiscCodec.createToIntFunction(
                        objectClass.getMethod("getMinimumDaysInFirstWeek")
                );
                Method method = objectClass.getMethod("getZone");
                this.getZone = LambdaMiscCodec.createFunction(method);
                this.getID = LambdaMiscCodec.createFunction(
                        method.getReturnType().getMethod("getID")
                );
            } catch (NoSuchMethodException e) {
                throw new JSONException("getMethod error", e);
            }
        }

        @Override
        public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            Object zone = getZone.apply(object);
            String zoneId = (String) getID.apply(zone);

            int minDaysInFirstWeek = getMinimumDaysInFirstWeek.applyAsInt(object);

            if (minDaysInFirstWeek == 4) {
                jsonWriter.startObject();
                jsonWriter.writeName("zoneId");
                jsonWriter.writeString(zoneId);
                jsonWriter.endObject();
            } else {
                jsonWriter.startObject();
                jsonWriter.writeName("minimumDaysInFirstWeek");
                jsonWriter.writeInt32(minDaysInFirstWeek);
                jsonWriter.writeName("zoneId");
                jsonWriter.writeString(zoneId);
                jsonWriter.endObject();
            }
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            Object zone = getZone.apply(object);
            String zoneId = (String) getID.apply(zone);

            int minDaysInFirstWeek = (Integer) getMinimumDaysInFirstWeek.applyAsInt(object);
            jsonWriter.startObject();
            jsonWriter.writeName("minimumDaysInFirstWeek");
            jsonWriter.writeInt32(minDaysInFirstWeek);
            jsonWriter.writeName("zoneId");
            jsonWriter.writeString(zoneId);
            jsonWriter.endObject();
        }
    }

    static class ISOChronologyWriter
            implements ObjectWriter {
        final Class objectClass;
        final Function getZone;
        final Function getID;

        ISOChronologyWriter(Class objectClass) {
            this.objectClass = objectClass;
            try {
                Method method = objectClass.getMethod("getZone");
                this.getZone = LambdaMiscCodec.createFunction(method);
                this.getID = LambdaMiscCodec.createFunction(
                        method.getReturnType().getMethod("getID")
                );
            } catch (NoSuchMethodException e) {
                throw new JSONException("getMethod error", e);
            }
        }

        @Override
        public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            Object zone = getZone.apply(object);
            String zoneId = (String) getID.apply(zone);

            jsonWriter.startObject();
            jsonWriter.writeName("zoneId");
            jsonWriter.writeString(zoneId);
            jsonWriter.endObject();
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            Object zone = getZone.apply(object);
            String zoneId = (String) getID.apply(zone);

            jsonWriter.startObject();
            jsonWriter.writeName("zoneId");
            jsonWriter.writeString(zoneId);
            jsonWriter.endObject();
        }
    }

    static class LocalDateReader
            implements ObjectReader {
        final Class objectClass;
        final Constructor constructor3;
        final Constructor constructor4;

        final Class classISOChronology;
        final Class classChronology;
        final Object utc;

        LocalDateReader(Class objectClass) {
            this.objectClass = objectClass;
            try {
                ClassLoader classLoader = objectClass.getClassLoader();
                classChronology = classLoader.loadClass("org.joda.time.Chronology");

                constructor3 = objectClass.getConstructor(int.class, int.class, int.class);
                constructor4 = objectClass.getConstructor(int.class, int.class, int.class, classChronology);

                classISOChronology = classLoader.loadClass("org.joda.time.chrono.ISOChronology");
                utc = classISOChronology.getMethod("getInstance").invoke(null);
            } catch (ClassNotFoundException
                    | NoSuchMethodException
                    | IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new JSONException("create LocalDateWriter error", e);
            }
        }

        @Override
        public Class getObjectClass() {
            return objectClass;
        }

        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            if (jsonReader.nextIfNull()) {
                return null;
            }

            LocalDate localDate = jsonReader.readLocalDate();
            if (localDate == null) {
                return null;
            }

            try {
                return constructor4.newInstance(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), null);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new JSONException(jsonReader.info("read org.joda.time.LocalDate error"), e);
            }
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            byte type = jsonReader.getType();

            if (type == BC_LOCAL_DATE) {
                LocalDate localDate = jsonReader.readLocalDate();
                try {
                    return constructor3.newInstance(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new JSONException(jsonReader.info("read org.joda.time.LocalDate error"), e);
                }
            }

            if (jsonReader.isObject()) {
                Integer year = null, month = null, day = null;
                Object chronology = null;
                jsonReader.nextIfObjectStart();
                for (; ; ) {
                    if (jsonReader.nextIfObjectEnd()) {
                        break;
                    }
                    long fieldNameHashCode = jsonReader.readFieldNameHashCode();
                    if (fieldNameHashCode == HASH_YEAR) {
                        year = jsonReader.readInt32Value();
                    } else if (fieldNameHashCode == HASH_MONTH) {
                        month = jsonReader.readInt32Value();
                    } else if (fieldNameHashCode == HASH_DAY) {
                        day = jsonReader.readInt32Value();
                    } else if (fieldNameHashCode == HASH_CHRONOLOGY) {
                        chronology = jsonReader.read(classChronology);
                    } else {
                        throw new JSONException(jsonReader.info("not support fieldName " + jsonReader.getFieldName()));
                    }
                }

                try {
                    return constructor4.newInstance(year, month, day, chronology);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new JSONException(jsonReader.info("read org.joda.time.LocalDate error"), e);
                }
            }

            throw new JSONException(jsonReader.info("not support " + JSONB.typeName(type)));
        }
    }

    static class LocalDateWriter
            extends DateTimeCodec
            implements ObjectWriter {
        final Class objectClass;
        final ToIntFunction getYear;
        final ToIntFunction getMonthOfYear;
        final ToIntFunction getDayOfMonth;
        final Function getChronology;

        final Class isoChronology;
        final Object utc;

        LocalDateWriter(Class objectClass, String format) {
            super(format);

            this.objectClass = objectClass;
            try {
                ClassLoader classLoader = objectClass.getClassLoader();
                isoChronology = classLoader.loadClass("org.joda.time.chrono.ISOChronology");
                Object instance = isoChronology.getMethod("getInstance").invoke(null);
                utc = isoChronology.getMethod("withUTC").invoke(instance);

                getYear = LambdaMiscCodec.createToIntFunction(
                        objectClass.getMethod("getYear")
                );
                getMonthOfYear = LambdaMiscCodec.createToIntFunction(
                        objectClass.getMethod("getMonthOfYear")
                );
                getDayOfMonth = LambdaMiscCodec.createToIntFunction(
                        objectClass.getMethod("getDayOfMonth")
                );
                getChronology = LambdaMiscCodec.createFunction(
                        objectClass.getMethod("getChronology")
                );
            } catch (ClassNotFoundException
                    | NoSuchMethodException
                    | IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new JSONException("create LocalDateWriter error", e);
            }
        }

        @Override
        public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            int year = getYear.applyAsInt(object);
            int monthOfYear = getMonthOfYear.applyAsInt(object);
            int dayOfMonth = getDayOfMonth.applyAsInt(object);
            Object chronology = getChronology.apply(object);

            if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
                jsonWriter.writeTypeName(TypeUtils.getTypeName(object.getClass()));
            }

            if (chronology == utc || chronology == null) {
                jsonWriter.writeLocalDate(LocalDate.of(year, monthOfYear, dayOfMonth));
                return;
            }

            jsonWriter.startObject();

            jsonWriter.writeName("year");
            jsonWriter.writeInt32(year);

            jsonWriter.writeName("month");
            jsonWriter.writeInt32(monthOfYear);

            jsonWriter.writeName("day");
            jsonWriter.writeInt32(dayOfMonth);

            jsonWriter.writeName("chronology");
            jsonWriter.writeAny(chronology);

            jsonWriter.endObject();
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            int year = getYear.applyAsInt(object);
            int monthOfYear = getMonthOfYear.applyAsInt(object);
            int dayOfMonth = getDayOfMonth.applyAsInt(object);
            Object chronology = getChronology.apply(object);

            if (chronology == utc || chronology == null) {
                LocalDate localDate = LocalDate.of(year, monthOfYear, dayOfMonth);

                DateTimeFormatter formatter = this.getDateFormatter();
                if (formatter == null) {
                    formatter = jsonWriter.context.getDateFormatter();
                }

                if (formatter == null) {
                    jsonWriter.writeLocalDate(localDate);
                    return;
                }

                String str = formatter.format(localDate);
                jsonWriter.writeString(str);
                return;
            }

            jsonWriter.startObject();

            jsonWriter.writeName("year");
            jsonWriter.writeInt32(year);

            jsonWriter.writeName("month");
            jsonWriter.writeInt32(monthOfYear);

            jsonWriter.writeName("day");
            jsonWriter.writeInt32(dayOfMonth);

            jsonWriter.writeName("chronology");
            jsonWriter.writeAny(chronology);

            jsonWriter.endObject();
        }
    }

    static class LocalDateTimeReader
            implements ObjectReader {
        final Class objectClass;
        final Constructor constructor7;
        final Constructor constructor8;

        final Class classISOChronology;
        final Class classChronology;
        final Object utc;

        LocalDateTimeReader(Class objectClass) {
            this.objectClass = objectClass;
            try {
                ClassLoader classLoader = objectClass.getClassLoader();
                classChronology = classLoader.loadClass("org.joda.time.Chronology");

                constructor7 = objectClass.getConstructor(int.class, int.class, int.class, int.class, int.class, int.class, int.class);
                constructor8 = objectClass.getConstructor(int.class, int.class, int.class, int.class, int.class, int.class, int.class, classChronology);

                classISOChronology = classLoader.loadClass("org.joda.time.chrono.ISOChronology");
                utc = classISOChronology.getMethod("getInstance").invoke(null);
            } catch (ClassNotFoundException
                    | NoSuchMethodException
                    | IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new JSONException("create LocalDateWriter error", e);
            }
        }

        @Override
        public Class getObjectClass() {
            return objectClass;
        }

        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            if (jsonReader.isString() || jsonReader.isInt()) {
                LocalDateTime ldt = jsonReader.readLocalDateTime();
                if (ldt == null) {
                    return null;
                }

                try {
                    return constructor7.newInstance(ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth(), ldt.getHour(), ldt.getMinute(), ldt.getSecond(), ldt.getNano() / 1000_000);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new JSONException(jsonReader.info("read org.joda.time.LocalDate error"), e);
                }
            }

            throw new JSONException(jsonReader.info("not support"));
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            byte type = jsonReader.getType();

            if (type == BC_LOCAL_DATE) {
                LocalDate localDate = jsonReader.readLocalDate();
                try {
                    return constructor7.newInstance(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), 0, 0, 0, 0);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new JSONException(jsonReader.info("read org.joda.time.LocalDate error"), e);
                }
            }

            if (type == BC_LOCAL_DATETIME) {
                LocalDateTime ldt = jsonReader.readLocalDateTime();
                try {
                    return constructor7.newInstance(ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth(), ldt.getHour(), ldt.getMinute(), ldt.getSecond(), ldt.getNano() / 1000_000);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new JSONException(jsonReader.info("read org.joda.time.LocalDate error"), e);
                }
            }

            if (jsonReader.isObject()) {
                Integer year = null, month = null, day = null, hour = null, minute = null, second = null, millis = null;
                Object chronology = null;
                jsonReader.nextIfObjectStart();
                for (; ; ) {
                    if (jsonReader.nextIfObjectEnd()) {
                        break;
                    }
                    long fieldNameHashCode = jsonReader.readFieldNameHashCode();
                    if (fieldNameHashCode == HASH_YEAR) {
                        year = jsonReader.readInt32Value();
                    } else if (fieldNameHashCode == HASH_MONTH) {
                        month = jsonReader.readInt32Value();
                    } else if (fieldNameHashCode == HASH_DAY) {
                        day = jsonReader.readInt32Value();
                    } else if (fieldNameHashCode == HASH_HOUR) {
                        hour = jsonReader.readInt32Value();
                    } else if (fieldNameHashCode == HASH_MINUTE) {
                        minute = jsonReader.readInt32Value();
                    } else if (fieldNameHashCode == HASH_SECOND) {
                        second = jsonReader.readInt32Value();
                    } else if (fieldNameHashCode == HASH_MILLIS) {
                        millis = jsonReader.readInt32Value();
                    } else if (fieldNameHashCode == HASH_CHRONOLOGY) {
                        chronology = jsonReader.read(classChronology);
                    } else {
                        throw new JSONException(jsonReader.info("not support fieldName " + jsonReader.getFieldName()));
                    }
                }

                try {
                    return constructor8.newInstance(year, month, day, hour, minute, second, millis, chronology);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new JSONException(jsonReader.info("read org.joda.time.LocalDate error"), e);
                }
            }

            throw new JSONException(jsonReader.info("not support " + JSONB.typeName(type)));
        }
    }

    static class LocalDateTimeWriter
            extends DateTimeCodec
            implements ObjectWriter {
        final Class objectClass;

        final Method getYear;
        final Method getMonthOfYear;
        final Method getDayOfMonth;

        final ToIntFunction getHourOfDay;
        final ToIntFunction getMinuteOfHour;
        final ToIntFunction getSecondOfMinute;
        final ToIntFunction getMillisOfSecond;

        final Function getChronology;

        final Class isoChronology;
        final Object utc;

        LocalDateTimeWriter(Class objectClass, String format) {
            super(format);

            this.objectClass = objectClass;
            try {
                ClassLoader classLoader = objectClass.getClassLoader();
                isoChronology = classLoader.loadClass("org.joda.time.chrono.ISOChronology");
                Object instance = isoChronology.getMethod("getInstance").invoke(null);
                utc = isoChronology.getMethod("withUTC").invoke(instance);

                getYear = objectClass.getMethod("getYear");
                getMonthOfYear = objectClass.getMethod("getMonthOfYear");
                getDayOfMonth = objectClass.getMethod("getDayOfMonth");

                getHourOfDay = LambdaMiscCodec.createToIntFunction(
                        objectClass.getMethod("getHourOfDay")
                );
                getMinuteOfHour = LambdaMiscCodec.createToIntFunction(
                        objectClass.getMethod("getMinuteOfHour")
                );
                getSecondOfMinute = LambdaMiscCodec.createToIntFunction(
                        objectClass.getMethod("getSecondOfMinute")
                );
                getMillisOfSecond = LambdaMiscCodec.createToIntFunction(
                        objectClass.getMethod("getMillisOfSecond")
                );

                getChronology = LambdaMiscCodec.createFunction(
                        objectClass.getMethod("getChronology")
                );
            } catch (ClassNotFoundException
                    | NoSuchMethodException
                    | IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new JSONException("create LocalDateWriter error", e);
            }
        }

        @Override
        public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            try {
                int year = (Integer) getYear.invoke(object);
                int monthOfYear = (Integer) getMonthOfYear.invoke(object);
                int dayOfMonth = (Integer) getDayOfMonth.invoke(object);

                int hour = getHourOfDay.applyAsInt(object);
                int minute = getMinuteOfHour.applyAsInt(object);
                int second = getSecondOfMinute.applyAsInt(object);
                int millis = getMillisOfSecond.applyAsInt(object);

                Object chronology = getChronology.apply(object);

                if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
                    jsonWriter.writeTypeName(TypeUtils.getTypeName(object.getClass()));
                }

                if (chronology == utc || chronology == null) {
                    jsonWriter.writeLocalDateTime(
                            LocalDateTime.of(year, monthOfYear, dayOfMonth, hour, minute, second, millis * 1000000));
                    return;
                }

                jsonWriter.startObject();

                jsonWriter.writeName("year");
                jsonWriter.writeInt32(year);

                jsonWriter.writeName("month");
                jsonWriter.writeInt32(monthOfYear);

                jsonWriter.writeName("day");
                jsonWriter.writeInt32(dayOfMonth);

                jsonWriter.writeName("hour");
                jsonWriter.writeInt32(hour);

                jsonWriter.writeName("minute");
                jsonWriter.writeInt32(minute);

                jsonWriter.writeName("second");
                jsonWriter.writeInt32(second);

                jsonWriter.writeName("millis");
                jsonWriter.writeInt32(millis);

                jsonWriter.writeName("chronology");
                jsonWriter.writeAny(chronology);

                jsonWriter.endObject();
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new JSONException("write LocalDateWriter error", e);
            }
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            try {
                int year = (Integer) getYear.invoke(object);
                int monthOfYear = (Integer) getMonthOfYear.invoke(object);
                int dayOfMonth = (Integer) getDayOfMonth.invoke(object);

                int hour = getHourOfDay.applyAsInt(object);
                int minute = getMinuteOfHour.applyAsInt(object);
                int second = getSecondOfMinute.applyAsInt(object);
                int millis = getMillisOfSecond.applyAsInt(object);

                Object chronology = getChronology.apply(object);

                if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
                    jsonWriter.writeTypeName(TypeUtils.getTypeName(object.getClass()));
                }

                if (chronology == utc || chronology == null) {
                    int nanoOfSecond = millis * 1000_000;
                    LocalDateTime ldt = LocalDateTime.of(year, monthOfYear, dayOfMonth, hour, minute, second, nanoOfSecond);

                    DateTimeFormatter formatter = this.getDateFormatter();
                    if (formatter == null) {
                        formatter = jsonWriter.context.getDateFormatter();
                    }

                    if (formatter == null) {
                        jsonWriter.writeLocalDateTime(ldt);
                        return;
                    }

                    String str = formatter.format(ldt);
                    jsonWriter.writeString(str);
                    return;
                }

                jsonWriter.startObject();

                jsonWriter.writeName("year");
                jsonWriter.writeInt32(year);

                jsonWriter.writeName("month");
                jsonWriter.writeInt32(monthOfYear);

                jsonWriter.writeName("day");
                jsonWriter.writeInt32(dayOfMonth);

                jsonWriter.writeName("hour");
                jsonWriter.writeInt32(hour);

                jsonWriter.writeName("minute");
                jsonWriter.writeInt32(minute);

                jsonWriter.writeName("second");
                jsonWriter.writeInt32(second);

                jsonWriter.writeName("millis");
                jsonWriter.writeInt32(millis);

                jsonWriter.writeName("chronology");
                jsonWriter.writeAny(chronology);

                jsonWriter.endObject();
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new JSONException("write LocalDateWriter error", e);
            }
        }
    }
}
