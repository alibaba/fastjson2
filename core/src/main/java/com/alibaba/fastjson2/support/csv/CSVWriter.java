package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.util.*;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;

import static com.alibaba.fastjson2.util.DateUtils.DEFAULT_ZONE_ID;

public abstract class CSVWriter
        implements Closeable, Flushable {
    private long features;

    final ZoneId zoneId;

    int off;

    CSVWriter(ZoneId zoneId, Feature... features) {
        for (Feature feature : features) {
            this.features |= feature.mask;
        }

        this.zoneId = zoneId;
    }

    public static CSVWriter of() {
        return of(new ByteArrayOutputStream());
    }

    public static CSVWriter of(File file) throws FileNotFoundException {
        return of(new FileOutputStream(file), StandardCharsets.UTF_8);
    }

    public static CSVWriter of(File file, Charset charset) throws FileNotFoundException {
        return of(new FileOutputStream(file), charset);
    }

    public final void writeLineObject(Object object) {
        if (object == null) {
            this.writeLine();
            return;
        }
        ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
        Class<?> objectClass = object.getClass();
        ObjectWriter objectWriter = provider.getObjectWriter(objectClass);
        if (objectWriter instanceof ObjectWriterAdapter) {
            ObjectWriterAdapter adapter = (ObjectWriterAdapter) objectWriter;
            List<FieldWriter> fieldWriters = adapter.getFieldWriters();
            if (fieldWriters.size() == 1 && (fieldWriters.get(0).features & FieldInfo.VALUE_MASK) != 0) {
                Object fieldValue = fieldWriters.get(0).getFieldValue(object);
                writeLineObject(fieldValue);
                return;
            }

            Object[] values = new Object[fieldWriters.size()];
            for (int i = 0; i < fieldWriters.size(); i++) {
                values[i] = fieldWriters.get(i).getFieldValue(object);
            }
            writeLine(values);
        } else {
            writeLine(object);
        }
    }

    public final void writeDate(Date date) {
        if (date == null) {
            return;
        }
        long millis = date.getTime();
        writeDate(millis);
    }

    public final void writeInstant(Instant instant) {
        if (instant == null) {
            return;
        }

        int nano = instant.getNano();
        if (nano % 1000000 == 0) {
            long millis = instant.toEpochMilli();
            writeDate(millis);
            return;
        }

        if ((features & Feature.AlwaysQuoteStrings.mask) != 0) {
            writeQuote();
        }

        LocalDateTime ldt = instant.atZone(zoneId).toLocalDateTime();
        writeLocalDateTime(ldt);
    }

    public void writeLocalDate(LocalDate date) {
        if (date == null) {
            return;
        }
        String str = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
        writeRaw(str);
    }

    public abstract void writeLocalDateTime(LocalDateTime instant);

//    protected abstract void writeDirect(byte[] bytes, int off, int len);

    public final void writeLine(int columnCount, IntFunction function) {
        for (int i = 0; i < columnCount; i++) {
            Object value = function.apply(i);

            if (i != 0) {
                writeComma();
            }

            writeValue(value);
        }

        writeLine();
    }

    public final void writeLine(List values) {
        for (int i = 0; i < values.size(); i++) {
            if (i != 0) {
                writeComma();
            }

            writeValue(values.get(i));
        }

        writeLine();
    }

    public final void writeLine(Object... values) {
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                writeComma();
            }

            writeValue(values[i]);
        }

        writeLine();
    }

    public abstract void writeComma();

    protected abstract void writeQuote();

    public abstract void writeLine();

    public void writeValue(Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof Optional) {
            Optional optional = (Optional) value;
            if (!optional.isPresent()) {
                return;
            }
            value = optional.get();
        }

        if (value instanceof Integer) {
            this.writeInt32(((Integer) value).intValue());
        } else if (value instanceof Long) {
            this.writeInt64(((Long) value).longValue());
        } else if (value instanceof String) {
            writeString((String) value);
        } else if (value instanceof Boolean) {
            boolean booleanValue = ((Boolean) value).booleanValue();
            writeBoolean(booleanValue);
        } else if (value instanceof Float) {
            float floatValue = ((Float) value).floatValue();
            writeFloat(floatValue);
        } else if (value instanceof Double) {
            writeDouble((Double) value);
        } else if (value instanceof Short) {
            this.writeInt32(((Short) value).intValue());
        } else if (value instanceof Byte) {
            this.writeInt32(((Byte) value).intValue());
        } else if (value instanceof BigDecimal) {
            writeDecimal((BigDecimal) value);
        } else if (value instanceof BigInteger) {
            writeBigInteger((BigInteger) value);
        } else if (value instanceof Date) {
            writeDate((Date) value);
        } else if (value instanceof Instant) {
            writeInstant((Instant) value);
        } else if (value instanceof LocalDate) {
            writeLocalDate((LocalDate) value);
        } else if (value instanceof LocalDateTime) {
            writeLocalDateTime((LocalDateTime) value);
        } else {
            String str = value.toString();
            writeString(str);
        }
    }

    public void writeBigInteger(BigInteger value) {
        if (value == null) {
            return;
        }

        String str = value.toString();
        writeRaw(str);
    }

    public abstract void writeBoolean(boolean booleanValue);

    public abstract void writeInt64(long longValue);

    public final void writeDate(long millis) {
        ZoneId zoneId = this.zoneId;

        final int SECONDS_PER_DAY = 60 * 60 * 24;
        long epochSecond = Math.floorDiv(millis, 1000L);
        int offsetTotalSeconds;
        if (zoneId == DateUtils.SHANGHAI_ZONE_ID || zoneId.getRules() == DateUtils.SHANGHAI_ZONE_RULES) {
            offsetTotalSeconds = DateUtils.getShanghaiZoneOffsetTotalSeconds(epochSecond);
        } else {
            Instant instant = Instant.ofEpochMilli(millis);
            offsetTotalSeconds = zoneId.getRules().getOffset(instant).getTotalSeconds();
        }

        long localSecond = epochSecond + offsetTotalSeconds;
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
            if (yearEst < Year.MIN_VALUE || yearEst > Year.MAX_VALUE) {
                throw new DateTimeException("Invalid year " + yearEst);
            }

            year = (int) yearEst;
        }

        int hour, minute, second;
        {
            final int MINUTES_PER_HOUR = 60;
            final int SECONDS_PER_MINUTE = 60;
            final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

            long secondOfDay = secsOfDay;
            if (secondOfDay < 0 || secondOfDay > 86399) {
                throw new DateTimeException("Invalid secondOfDay " + secondOfDay);
            }
            int hours = (int) (secondOfDay / SECONDS_PER_HOUR);
            secondOfDay -= hours * SECONDS_PER_HOUR;
            int minutes = (int) (secondOfDay / SECONDS_PER_MINUTE);
            secondOfDay -= minutes * SECONDS_PER_MINUTE;

            hour = hours;
            minute = minutes;
            second = (int) secondOfDay;
        }

        if (year >= 0 && year <= 9999) {
            int mos = (int) Math.floorMod(millis, 1000L);
            if (mos == 0) {
                if (hour == 0 && minute == 0 && second == 0) {
                    writeDateYYYMMDD10(year, month, dayOfMonth);
                } else {
                    writeDateTime19(year, month, dayOfMonth, hour, minute, second);
                }
                return;
            }
        }

        String str = DateUtils.toString(millis, false, zoneId);
        writeRaw(str);
    }

    public abstract void writeDateYYYMMDD10(int year, int month, int dayOfMonth);

    public abstract void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second);

    public abstract void writeString(String value);

    public abstract void writeInt32(int intValue);

    public abstract void writeDouble(double value);

    public abstract void writeFloat(float value);

    public abstract void flush();

    public abstract void writeString(byte[] utf8);

    public abstract void writeDecimal(BigDecimal value);

    public abstract void writeDecimal(long unscaledVal, int scale);

//    protected abstract void writeRaw(byte[] strBytes);

    protected abstract void writeRaw(String str);

    @Override
    public abstract void close() throws IOException;

    public static CSVWriter of(OutputStream out, Feature... features) {
        return new CSVWriterUTF8(out, StandardCharsets.UTF_8, DEFAULT_ZONE_ID, features);
    }

    public static CSVWriter of(OutputStream out, Charset charset) {
        return of(out, charset, DEFAULT_ZONE_ID);
    }

    public static CSVWriter of(OutputStream out, Charset charset, ZoneId zoneId) {
        if (charset == StandardCharsets.UTF_16
                || charset == StandardCharsets.UTF_16LE
                || charset == StandardCharsets.UTF_16BE
        ) {
            return of(new OutputStreamWriter(out, charset), zoneId);
        }

        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        return new CSVWriterUTF8(out, charset, zoneId);
    }

    public static CSVWriter of(Writer out) {
        return new CSVWriterUTF16(out, DEFAULT_ZONE_ID);
    }

    public static CSVWriter of(Writer out, ZoneId zoneId) {
        return new CSVWriterUTF16(out, zoneId);
    }

    public enum Feature {
        AlwaysQuoteStrings(1);

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }
    }
}
