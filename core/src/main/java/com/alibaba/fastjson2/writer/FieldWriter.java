package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static java.time.temporal.ChronoField.SECOND_OF_DAY;
import static java.time.temporal.ChronoField.YEAR;

public interface FieldWriter<T> extends Comparable {
    String getFieldName();

    Class getFieldClass();

    default boolean isFieldClassSerializable() {
        return false;
    }

    default boolean unwrapped() {
        return false;
    }

    Type getFieldType();

    default Type getItemType() {
        return null;
    }

    default Class getItemClass() {
        return null;
    }

    default int ordinal() {
        return -1;
    }

    default Field getField() {
        return null;
    }

    default Method getMethod() {
        return null;
    }

    default Member getFieldOrMethod() {
        Field field = getField();
        if (field != null) {
            return field;
        }
        return getMethod();
    }

    default long getFeatures() {
        return 0;
    }

    default Object getFieldValue(T object) {
        throw new UnsupportedOperationException();
    }

    @Override
    default int compareTo(Object o) {
        FieldWriter otherFieldWriter = (FieldWriter) o;

        String thisName = this.getFieldName();
        String otherName = otherFieldWriter.getFieldName();

        int nameCompare = thisName.compareTo(otherName);

        if (nameCompare != 0) {
            int thisOrdinal = this.ordinal();
            int otherOrdinal = otherFieldWriter.ordinal();
            if (thisOrdinal < otherOrdinal) {
                return -1;
            }
            if (thisOrdinal > otherOrdinal) {
                return 1;
            }
        } else {
            Member thisMember = this.getFieldOrMethod();
            Member otherMember = otherFieldWriter.getFieldOrMethod();

            if (thisMember instanceof Field && otherMember instanceof Method) {
                return -1;
            }

            if (thisMember instanceof Method && otherMember instanceof Field) {
                return 1;
            }
        }

        if (nameCompare != 0) {
            return nameCompare;
        }

        Class otherFieldClass = otherFieldWriter.getFieldClass();
        Class thisFieldClass = this.getFieldClass();
        if (thisFieldClass != otherFieldClass && thisFieldClass != null && otherFieldClass != null) {
            if (thisFieldClass.isAssignableFrom(otherFieldClass)) {
                return 1;
            } else if (otherFieldClass.isAssignableFrom(thisFieldClass)) {
                return -1;
            }
        }

        return nameCompare;
    }

    void writeFieldName(JSONWriter jsonWriter);

    void writeEnumJSONB(JSONWriter jsonWriter, Enum e);

    default void writeEnum(JSONWriter jsonWriter, Enum e) {
        writeFieldName(jsonWriter);
        jsonWriter.writeEnum(e);
    }

    default void writeBinary(JSONWriter jsonWriter, byte[] value) {
        if (value == null && !jsonWriter.isWriteNulls()) {
            return;
        }

        writeFieldName(jsonWriter);
        if ("base64".equals(getFormat())) {
            jsonWriter.writeBase64(value);
        } else {
            jsonWriter.writeBinary(value);
        }
    }

    default void writeInt16(JSONWriter jsonWriter, short[] value) {
        if (value == null && !jsonWriter.isWriteNulls()) {
            return;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeInt16(value);
    }

    default void writeInt32(JSONWriter jsonWriter, int value) {
        writeFieldName(jsonWriter);
        jsonWriter.writeInt32(value);
    }

    default void writeInt64(JSONWriter jsonWriter, long value) {
        writeFieldName(jsonWriter);
        jsonWriter.writeInt64(value);
    }

    default void writeString(JSONWriter jsonWriter, String value) {
        writeFieldName(jsonWriter);
        jsonWriter.writeString(value);
    }

    default void writeString(JSONWriter jsonWriter, char[] value) {
        if (value == null && !jsonWriter.isWriteNulls()) {
            return;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeString(value);
    }

    default void writeFloat(JSONWriter jsonWriter, float value) {
        writeFieldName(jsonWriter);
        jsonWriter.writeFloat(value);
    }

    default void writeDouble(JSONWriter jsonWriter, double value) {
        writeFieldName(jsonWriter);
        jsonWriter.writeDouble(value);
    }

    default void writeDate(JSONWriter jsonWriter, Date value) {
        if (value == null) {
            writeFieldName(jsonWriter);
            jsonWriter.writeNull();
            return;
        }

        writeDate(jsonWriter, value.getTime());
    }

    default void writeBool(JSONWriter jsonWriter, boolean value) {
        throw new UnsupportedOperationException();
    }

    default void writeBool(JSONWriter jsonWriter, boolean[] value) {
        if (value == null && !jsonWriter.isWriteNulls()) {
            return;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeBool(value);
    }

    default void writeFloat(JSONWriter jsonWriter, float[] value) {
        if (value == null && !jsonWriter.isWriteNulls()) {
            return;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeFloat(value);
    }

    default void writeDouble(JSONWriter jsonWriter, double[] value) {
        if (value == null && !jsonWriter.isWriteNulls()) {
            return;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeDouble(value);
    }

    default void writeDate(JSONWriter jsonWriter, boolean writeFieldName, Date value) {
        if (value == null) {
            if (writeFieldName) {
                writeFieldName(jsonWriter);
            }

            jsonWriter.writeNull();
            return;
        }

        writeDate(jsonWriter, writeFieldName, value.getTime());
    }

    default void writeDate(JSONWriter jsonWriter, long millis) {
        writeDate(jsonWriter, true, millis);
    }

    default boolean isDateFormatMillis() {
        return false;
    }

    default boolean isDateFormatISO8601() {
        return false;
    }

    default String getFormat() {
        return null;
    }

    default void writeDate(JSONWriter jsonWriter, boolean writeFieldName, long millis) {
        if (jsonWriter.isJSONB()) {
            jsonWriter.writeMillis(millis);
            return;
        }

        final int SECONDS_PER_DAY = 60 * 60 * 24;

        JSONWriter.Context ctx = jsonWriter.getContext();
        if (isDateFormatMillis() || ctx.isDateFormatMillis()) {
            if (writeFieldName) {
                writeFieldName(jsonWriter);
            }
            jsonWriter.writeInt64(millis);
            return;
        }

        ZoneId zoneId = ctx.getZoneId();

        String dateFormat = ctx.getDateFormat();
        if (dateFormat == null) {
            Instant instant = Instant.ofEpochMilli(millis);
            long epochSecond = instant.getEpochSecond();
            ZoneOffset offset = zoneId
                    .getRules()
                    .getOffset(instant);

            long localSecond = epochSecond + offset.getTotalSeconds();
            long localEpochDay = Math.floorDiv(localSecond, (long) SECONDS_PER_DAY);
            int secsOfDay = (int) Math.floorMod(localSecond, (long) SECONDS_PER_DAY);
            int year, month, dayOfMonth;
            {
                final int DAYS_PER_CYCLE = 146097;
                final long DAYS_0000_TO_1970 = (DAYS_PER_CYCLE * 5L) - (30L * 365L + 7L);

                long zeroDay = localEpochDay + DAYS_0000_TO_1970;
                // find the march-based year
                zeroDay -= 60;  // adjust to 0000-03-01 so leap day is at end of four year cycle
                long adjust = 0;
                if (zeroDay < 0) {
                    // adjust negative years to positive for calculation
                    long adjustCycles = (zeroDay + 1) / DAYS_PER_CYCLE - 1;
                    adjust = adjustCycles * 400;
                    zeroDay += -adjustCycles * DAYS_PER_CYCLE;
                }
                long yearEst = (400 * zeroDay + 591) / DAYS_PER_CYCLE;
                long doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
                if (doyEst < 0) {
                    // fix estimate
                    yearEst--;
                    doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
                }
                yearEst += adjust;  // reset any negative year
                int marchDoy0 = (int) doyEst;

                // convert march-based values back to january-based
                int marchMonth0 = (marchDoy0 * 5 + 2) / 153;
                month = (marchMonth0 + 2) % 12 + 1;
                dayOfMonth = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1;
                yearEst += marchMonth0 / 10;

                // check year now we are certain it is correct
                year = YEAR.checkValidIntValue(yearEst);
            }

            int hour, minute, second;
            {
                final int MINUTES_PER_HOUR = 60;
                final int SECONDS_PER_MINUTE = 60;
                final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

                long secondOfDay = secsOfDay;
                SECOND_OF_DAY.checkValidValue(secondOfDay);
                int hours = (int) (secondOfDay / SECONDS_PER_HOUR);
                secondOfDay -= hours * SECONDS_PER_HOUR;
                int minutes = (int) (secondOfDay / SECONDS_PER_MINUTE);
                secondOfDay -= minutes * SECONDS_PER_MINUTE;

                hour = hours;
                minute = minutes;
                second = (int) secondOfDay;
            }

            if (writeFieldName) {
                writeFieldName(jsonWriter);
            }
            jsonWriter.writeDateTime19(year, month, dayOfMonth, hour, minute, second);
        } else {
            ZonedDateTime zdt = ZonedDateTime
                    .ofInstant(
                            Instant.ofEpochMilli(millis), zoneId);

            if (isDateFormatISO8601() || ctx.isDateFormatISO8601()) {
                int year = zdt.getYear();
                int month = zdt.getMonthValue();
                int dayOfMonth = zdt.getDayOfMonth();
                int hour = zdt.getHour();
                int minute = zdt.getMinute();
                int second = zdt.getSecond();
                int milliSeconds = zdt.getNano() / 1000_000;
                int offsetSeconds = zdt.getOffset().getTotalSeconds();
                jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, milliSeconds, offsetSeconds);
                return;
            }

            String str = ctx.getDateFormatter().format(zdt);

            if (writeFieldName) {
                writeFieldName(jsonWriter);
            }
            jsonWriter.writeString(str);
        }
    }

    default ObjectWriter getItemWriter(JSONWriter writer, Type itemType) {
        return writer
                .getObjectWriter(itemType, null);
    }

    void writeValue(JSONWriter jsonWriter, T object);

    boolean write(JSONWriter jsonWriter, T o);

    default ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        return jsonWriter.getObjectWriter(valueClass);
    }

    default void writeList(JSONWriter jsonWriter, boolean writeFieldName, List list) {
        throw new UnsupportedOperationException();
    }

    default void writeListStr(JSONWriter jsonWriter, boolean writeFieldName, List<String> list) {
        throw new UnsupportedOperationException();
    }
}
