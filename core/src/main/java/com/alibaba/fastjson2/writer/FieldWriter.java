package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.util.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.*;
import java.time.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteByteArrayAsBase64;
import static java.time.temporal.ChronoField.SECOND_OF_DAY;
import static java.time.temporal.ChronoField.YEAR;

public abstract class FieldWriter<T>
        implements Comparable {
    public final String fieldName;
    public final Type fieldType;
    public final Class fieldClass;
    public final long features;
    public final int ordinal;
    public final String format;
    public final String label;
    public final Field field;
    public final Method method;

    final long hashCode;
    final byte[] nameWithColonUTF8;
    final char[] nameWithColonUTF16;
    byte[] nameJSONB;
    long nameSymbolCache;

    final boolean fieldClassSerializable;
    final JSONWriter.Path rootParentPath;

    final boolean symbol;
    final boolean trim;
    final boolean raw;

    transient JSONWriter.Path path;

    FieldWriter(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method
    ) {
        this.fieldName = name;
        this.ordinal = ordinal;
        this.format = format;
        this.label = label;
        this.hashCode = Fnv.hashCode64(name);
        this.features = features;
        this.fieldType = TypeUtils.intern(fieldType);
        this.fieldClass = fieldClass;
        this.fieldClassSerializable = fieldClass != null && (Serializable.class.isAssignableFrom(fieldClass) || !Modifier.isFinal(fieldClass.getModifiers()));
        this.field = field;
        this.method = method;

        this.symbol = "symbol".equals(format);
        this.trim = "trim".equals(format);
        this.raw = (features & FieldInfo.RAW_VALUE_MASK) != 0;
        this.rootParentPath = new JSONWriter.Path(JSONWriter.Path.ROOT, name);

        int nameLength = name.length();
        int utflen = nameLength + 3;
        for (int i = 0; i < nameLength; ++i) {
            char c = name.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                // skip
            } else if (c > 0x07FF) {
                utflen += 2;
            } else {
                utflen += 1;
            }
        }

        byte[] bytes = new byte[utflen];
        int off = 0;
        bytes[off++] = '"';
        for (int i = 0; i < nameLength; ++i) {
            char c = name.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytes[off++] = (byte) c;
            } else if (c > 0x07FF) {
                // 2 bytes, 11 bits
                bytes[off++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytes[off++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytes[off++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            } else {
                bytes[off++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytes[off++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            }
        }
        bytes[off++] = '"';
        bytes[off++] = ':';

        nameWithColonUTF8 = bytes;

        nameWithColonUTF16 = new char[nameLength + 3];
        nameWithColonUTF16[0] = '"';
        name.getChars(0, name.length(), nameWithColonUTF16, 1);
        nameWithColonUTF16[nameWithColonUTF16.length - 2] = '"';
        nameWithColonUTF16[nameWithColonUTF16.length - 1] = ':';
    }

    public boolean isFieldClassSerializable() {
        return fieldClassSerializable;
    }

    public boolean isDateFormatMillis() {
        return false;
    }

    public boolean isDateFormatISO8601() {
        return false;
    }

    public void writeEnumJSONB(JSONWriter jsonWriter, Enum e) {
        throw new UnsupportedOperationException();
    }

    public ObjectWriter getInitWriter() {
        return null;
    }

    public boolean unwrapped() {
        return false;
    }

    public final void writeFieldName(JSONWriter jsonWriter) {
        if (jsonWriter.jsonb) {
            if (nameJSONB == null) {
                nameJSONB = JSONB.toBytes(fieldName);
            }

            SymbolTable symbolTable = jsonWriter.symbolTable;
            if (symbolTable != null) {
                int symbolTableIdentity = System.identityHashCode(symbolTable);

                int symbol;
                if (nameSymbolCache == 0) {
                    symbol = symbolTable.getOrdinalByHashCode(hashCode);
                    nameSymbolCache = ((long) symbol << 32) | symbolTableIdentity;
                } else {
                    int identity = (int) nameSymbolCache;
                    if (identity == symbolTableIdentity) {
                        symbol = (int) (nameSymbolCache >> 32);
                    } else {
                        symbol = symbolTable.getOrdinalByHashCode(hashCode);
                        nameSymbolCache = ((long) symbol << 32) | symbolTableIdentity;
                    }
                }

                if (symbol != -1) {
                    jsonWriter.writeSymbol(-symbol);
                    return;
                }
            }

            jsonWriter.writeNameRaw(nameJSONB, hashCode);
            return;
        }

        if (!jsonWriter.useSingleQuote) {
            if (jsonWriter.utf8) {
                jsonWriter.writeNameRaw(nameWithColonUTF8);
                return;
            }

            if (jsonWriter.utf16) {
                jsonWriter.writeNameRaw(nameWithColonUTF16);
                return;
            }
        }

        jsonWriter.writeName(fieldName);
        jsonWriter.writeColon();
    }

    public final JSONWriter.Path getRootParentPath() {
        return rootParentPath;
    }

    public final JSONWriter.Path getPath(JSONWriter.Path parent) {
        if (path == null) {
            return path = new JSONWriter.Path(parent, fieldName);
        }

        if (path.parent == parent) {
            return path;
        }

        return new JSONWriter.Path(parent, fieldName);
    }

    public Type getItemType() {
        return null;
    }

    public Class getItemClass() {
        return null;
    }

    @Override
    public String toString() {
        return fieldName;
    }

    public abstract Object getFieldValue(T object);

    @Override
    public int compareTo(Object o) {
        FieldWriter other = (FieldWriter) o;

        int thisOrdinal = this.ordinal;
        int otherOrdinal = other.ordinal;
        if (thisOrdinal < otherOrdinal) {
            return -1;
        }
        if (thisOrdinal > otherOrdinal) {
            return 1;
        }

        String thisName = this.fieldName;
        String otherName = other.fieldName;
        int nameCompare = thisName.compareTo(otherName);

        if (nameCompare != 0) {
            return nameCompare;
        }

        Member thisMember = this.field != null ? this.field : this.method;
        Member otherMember = other.field != null ? other.field : other.method;

        if (thisMember != null && otherMember != null) {
            Class otherDeclaringClass = otherMember.getDeclaringClass();
            Class thisDeclaringClass = thisMember.getDeclaringClass();
            if (thisDeclaringClass != otherDeclaringClass && thisDeclaringClass != null && otherDeclaringClass != null) {
                if (thisDeclaringClass.isAssignableFrom(otherDeclaringClass)) {
                    return 1;
                } else if (otherDeclaringClass.isAssignableFrom(thisDeclaringClass)) {
                    return -1;
                }
            }
        }

        if (thisMember instanceof Field && otherMember instanceof Method) {
            return -1;
        }

        if (thisMember instanceof Method && otherMember instanceof Field) {
            return 1;
        }

        Class otherFieldClass = other.fieldClass;
        Class thisFieldClass = this.fieldClass;
        if (thisFieldClass != otherFieldClass && thisFieldClass != null && otherFieldClass != null) {
            if (thisFieldClass.isAssignableFrom(otherFieldClass)) {
                return 1;
            } else if (otherFieldClass.isAssignableFrom(thisFieldClass)) {
                return -1;
            }
        }

        if (thisFieldClass == boolean.class && otherFieldClass != boolean.class) {
            return 1;
        }
        if (otherFieldClass == boolean.class && thisFieldClass == boolean.class) {
            return -1;
        }

        return nameCompare;
    }

    public void writeEnum(JSONWriter jsonWriter, Enum e) {
        writeFieldName(jsonWriter);
        jsonWriter.writeEnum(e);
    }

    public void writeBinary(JSONWriter jsonWriter, byte[] value) {
        if (value == null && !jsonWriter.isWriteNulls()) {
            return;
        }

        writeFieldName(jsonWriter);
        if ("base64".equals(format)
                || (format == null && (jsonWriter.getFeatures(this.features) & WriteByteArrayAsBase64.mask) != 0)
        ) {
            jsonWriter.writeBase64(value);
        } else if ("hex".equals(format)) {
            jsonWriter.writeHex(value);
        } else if ("gzip,base64".equals(format) || "gzip".equals(format)) {
            GZIPOutputStream gzipOut = null;
            try {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                if (value.length < 512) {
                    gzipOut = new GZIPOutputStream(byteOut, value.length);
                } else {
                    gzipOut = new GZIPOutputStream(byteOut);
                }
                gzipOut.write(value);
                gzipOut.finish();
                value = byteOut.toByteArray();
            } catch (IOException ex) {
                throw new JSONException("write gzipBytes error", ex);
            } finally {
                IOUtils.close(gzipOut);
            }

            jsonWriter.writeBase64(value);
        } else {
            jsonWriter.writeBinary(value);
        }
    }

    public void writeInt16(JSONWriter jsonWriter, short[] value) {
        if (value == null && !jsonWriter.isWriteNulls()) {
            return;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeInt16(value);
    }

    public void writeInt32(JSONWriter jsonWriter, int value) {
        writeFieldName(jsonWriter);
        jsonWriter.writeInt32(value);
    }

    public void writeInt64(JSONWriter jsonWriter, long value) {
        writeFieldName(jsonWriter);
        jsonWriter.writeInt64(value);
    }

    public void writeString(JSONWriter jsonWriter, String value) {
        writeFieldName(jsonWriter);

        if (value == null && (features & (JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask)) != 0) {
            jsonWriter.writeString("");
            return;
        }

        if (trim && value != null) {
            value = value.trim();
        }

        if (symbol && jsonWriter.jsonb) {
            jsonWriter.writeSymbol(value);
        } else {
            if (raw) {
                jsonWriter.writeRaw(value);
            } else {
                jsonWriter.writeString(value);
            }
        }
    }

    public void writeString(JSONWriter jsonWriter, char[] value) {
        if (value == null && !jsonWriter.isWriteNulls()) {
            return;
        }

        writeFieldName(jsonWriter);
        if (value == null) {
            jsonWriter.writeStringNull();
            return;
        }

        jsonWriter.writeString(value, 0, value.length);
    }

    public void writeFloat(JSONWriter jsonWriter, float value) {
        writeFieldName(jsonWriter);
        jsonWriter.writeFloat(value);
    }

    public void writeDouble(JSONWriter jsonWriter, double value) {
        writeFieldName(jsonWriter);
        jsonWriter.writeDouble(value);
    }

    public void writeBool(JSONWriter jsonWriter, boolean value) {
        throw new UnsupportedOperationException();
    }

    public void writeBool(JSONWriter jsonWriter, boolean[] value) {
        if (value == null && !jsonWriter.isWriteNulls()) {
            return;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeBool(value);
    }

    public void writeFloat(JSONWriter jsonWriter, float[] value) {
        if (value == null && !jsonWriter.isWriteNulls()) {
            return;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeFloat(value);
    }

    public void writeDouble(JSONWriter jsonWriter, double[] value) {
        if (value == null && !jsonWriter.isWriteNulls()) {
            return;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeDouble(value);
    }

    public void writeDate(JSONWriter jsonWriter, boolean writeFieldName, Date value) {
        if (value == null) {
            if (writeFieldName) {
                writeFieldName(jsonWriter);
            }

            jsonWriter.writeNull();
            return;
        }

        writeDate(jsonWriter, writeFieldName, value.getTime());
    }

    public void writeDate(JSONWriter jsonWriter, long millis) {
        writeDate(jsonWriter, true, millis);
    }

    public void writeDate(JSONWriter jsonWriter, boolean writeFieldName, long millis) {
        if (jsonWriter.jsonb) {
            jsonWriter.writeMillis(millis);
            return;
        }

        final int SECONDS_PER_DAY = 60 * 60 * 24;

        JSONWriter.Context ctx = jsonWriter.context;
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
                jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, milliSeconds, offsetSeconds, true);
                return;
            }

            String str = ctx.getDateFormatter().format(zdt);

            if (writeFieldName) {
                writeFieldName(jsonWriter);
            }
            jsonWriter.writeString(str);
        }
    }

    public ObjectWriter getItemWriter(JSONWriter writer, Type itemType) {
        return writer
                .getObjectWriter(itemType, null);
    }

    public abstract void writeValue(JSONWriter jsonWriter, T object);

    public abstract boolean write(JSONWriter jsonWriter, T o);

    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        return jsonWriter.getObjectWriter(valueClass);
    }

    public void writeList(JSONWriter jsonWriter, boolean writeFieldName, List list) {
        throw new UnsupportedOperationException();
    }

    public void writeListStr(JSONWriter jsonWriter, boolean writeFieldName, List<String> list) {
        throw new UnsupportedOperationException();
    }

    static ObjectWriter getObjectWriter(Type fieldType, Class fieldClass, String format, Locale locale, Class valueClass) {
        if (Map.class.isAssignableFrom(valueClass)) {
            if (fieldClass.isAssignableFrom(valueClass)) {
                return ObjectWriterImplMap.of(fieldType, valueClass);
            } else {
                return ObjectWriterImplMap.of(valueClass);
            }
        } else {
            if (Calendar.class.isAssignableFrom(valueClass)) {
                if (format == null || format.isEmpty()) {
                    return ObjectWriterImplCalendar.INSTANCE;
                }
                return new ObjectWriterImplCalendar(format, locale);
            }

            if (ZonedDateTime.class.isAssignableFrom(valueClass)) {
                if (format == null || format.isEmpty()) {
                    return ObjectWriterImplZonedDateTime.INSTANCE;
                } else {
                    return new ObjectWriterImplZonedDateTime(format, locale);
                }
            }

            if (LocalDateTime.class.isAssignableFrom(valueClass)) {
                ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(LocalDateTime.class);
                if (objectWriter != null && objectWriter != ObjectWriterImplLocalDateTime.INSTANCE) {
                    return objectWriter;
                }

                if (format == null || format.isEmpty()) {
                    return ObjectWriterImplLocalDateTime.INSTANCE;
                } else {
                    return new ObjectWriterImplLocalDateTime(format, locale);
                }
            }

            if (LocalDate.class.isAssignableFrom(valueClass)) {
                ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(LocalDate.class);
                if (objectWriter != null && objectWriter != ObjectWriterImplLocalDate.INSTANCE) {
                    return objectWriter;
                }

                if (format == null || format.isEmpty()) {
                    return ObjectWriterImplLocalDate.INSTANCE;
                } else {
                    return new ObjectWriterImplLocalDate(format, locale);
                }
            }

            if (LocalTime.class.isAssignableFrom(valueClass)) {
                ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(LocalTime.class);
                if (objectWriter != null && objectWriter != ObjectWriterImplLocalTime.INSTANCE) {
                    return objectWriter;
                }

                if (format == null || format.isEmpty()) {
                    return ObjectWriterImplLocalTime.INSTANCE;
                } else {
                    return new ObjectWriterImplLocalTime(format, locale);
                }
            }

            if (Instant.class == valueClass) {
                if (format == null || format.isEmpty()) {
                    return ObjectWriterImplInstant.INSTANCE;
                } else {
                    return new ObjectWriterImplInstant(format, locale);
                }
            }

            if (Optional.class == valueClass) {
                return ObjectWriterImplOptional.of(format, locale);
            }

            String className = valueClass.getName();
            switch (className) {
                case "java.sql.Time":
                    return JdbcSupport.createTimeWriter(format);
                case "java.sql.Date":
                    return new ObjectWriterImplDate(format, locale);
                case "java.sql.Timestamp":
                    return JdbcSupport.createTimestampWriter(valueClass, format);
                case "org.joda.time.LocalDate":
                    return JodaSupport.createLocalDateWriter(valueClass, format);
                case "org.joda.time.LocalDateTime":
                    return JodaSupport.createLocalDateTimeWriter(valueClass, format);
                default:
                    break;
            }
        }

        return null;
    }
}
