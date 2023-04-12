package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
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

public class CSVWriter
        implements Closeable, Flushable {
    private long features;

    static final byte[] BYTES_TRUE = "true".getBytes();
    static final byte[] BYTES_FALSE = "false".getBytes();
    static final byte[] BYTES_LONG_MIN = "-9223372036854775808".getBytes();
    static final byte[] BYTES_LONG_MIN_UN_SIGNED = "9223372036854775808".getBytes();

    final OutputStream out;
    final Charset charset;
    final ZoneId zoneId;

    byte[] bytes;
    int off;

    CSVWriter(OutputStream out, Charset charset, ZoneId zoneId, Feature... features) {
        for (Feature feature : features) {
            this.features |= feature.mask;
        }

        this.out = out;
        this.charset = charset;
        this.zoneId = zoneId;
        this.bytes = new byte[1024 * 64];
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

    public void writeRowObject(Object object) {
        if (object == null) {
            writeRow();
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
                writeRowObject(fieldValue);
                return;
            }

            Object[] values = new Object[fieldWriters.size()];
            for (int i = 0; i < fieldWriters.size(); i++) {
                values[i] = fieldWriters.get(i).getFieldValue(object);
            }
            writeRow(values);
        } else {
            writeRow(object);
        }
    }

    protected void writeDate(Date date) {
        if (date == null) {
            return;
        }
        long millis = date.getTime();
        writeDate(millis);
    }

    protected void writeInstant(Instant instant) {
        if (instant == null) {
            return;
        }

        ZonedDateTime zdt = instant.atZone(ZoneOffset.UTC);
        String str = DateTimeFormatter.ISO_ZONED_DATE_TIME.format(zdt);
        if ((features & Feature.AlwaysQuoteStrings.mask) != 0) {
            if (off + 1 == bytes.length) {
                flush();
            }
            bytes[off++] = (byte) '"';

            writeRaw(str);

            if (off + 1 == bytes.length) {
                flush();
            }
            bytes[off++] = (byte) '"';
        } else {
            writeRaw(str);
        }
    }

    protected void writeDate(LocalDate date) {
        if (date == null) {
            return;
        }
        String str = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
        writeRaw(str);
    }

    protected void writeDateTime(LocalDateTime instant) {
        if (instant == null) {
            return;
        }
        String str = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(instant);
        writeRaw(str);
    }

    protected void writeDirect(byte[] bytes, int off, int len) {
        try {
            out.write(bytes, off, len);
        } catch (IOException e) {
            throw new JSONException("write csv error", e);
        }
    }

    public void writeRow(int columnCount, IntFunction function) {
        for (int i = 0; i < columnCount; i++) {
            Object value = function.apply(i);

            if (i != 0) {
                if (off + 1 >= bytes.length) {
                    flush();
                }
                bytes[off++] = ',';
            }

            writeValue(value);
        }

        if (off + 1 >= bytes.length) {
            flush();
        }
        bytes[off++] = '\n';
    }

    public void writeRow(Object... values) {
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                if (off + 1 >= bytes.length) {
                    flush();
                }
                bytes[off++] = ',';
            }

            writeValue(values[i]);
        }

        if (off + 1 == bytes.length) {
            flush();
        }
        bytes[off++] = '\n';
    }

    public void writeComma() {
        if (off + 1 == bytes.length) {
            flush();
        }
        bytes[off++] = ',';
    }

    public void writeLine() {
        if (off + 1 == bytes.length) {
            flush();
        }
        bytes[off++] = '\n';
    }

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
            if (Float.isNaN(floatValue) || Float.isInfinite(floatValue)) {
                return;
            }

            if (off + 15 > this.bytes.length) {
                flush();
            }

            int size = RyuFloat.toString(floatValue, bytes, off);
            off += size;
        } else if (value instanceof Double) {
            writeDouble((Double) value);
        } else if (value instanceof Short) {
            this.writeInt32(((Short) value).intValue());
        } else if (value instanceof Byte) {
            this.writeInt32(((Byte) value).intValue());
        } else if (value instanceof BigDecimal) {
            writeDecimal((BigDecimal) value);
        } else if (value instanceof BigInteger) {
            String str = value.toString();
            byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);
            writeRaw(bytes);
        } else if (value instanceof Date) {
            writeDate((Date) value);
        } else if (value instanceof Instant) {
            writeInstant((Instant) value);
        } else if (value instanceof LocalDate) {
            writeDate((LocalDate) value);
        } else if (value instanceof LocalDateTime) {
            writeDateTime((LocalDateTime) value);
        } else {
            String str = value.toString();
            writeString(str);
        }
    }

    public void writeBoolean(boolean booleanValue) {
        byte[] valueBytes = booleanValue ? BYTES_TRUE : BYTES_FALSE;
        writeRaw(valueBytes);
    }

    public void writeInt64(long longValue) {
        if (longValue == Long.MIN_VALUE) {
            writeRaw(BYTES_LONG_MIN);
            return;
        }

        int size = (longValue < 0) ? IOUtils.stringSize(-longValue) + 1 : IOUtils.stringSize(longValue);

        int minCapacity = off + size;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        IOUtils.getChars(longValue, off + size, bytes);
        off += size;
    }

    public void writeDate(long millis) {
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

    public final void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        if (off + 10 >= this.bytes.length) {
            flush();
        }

        bytes[off++] = (byte) (year / 1000 + '0');
        bytes[off++] = (byte) ((year / 100) % 10 + '0');
        bytes[off++] = (byte) ((year / 10) % 10 + '0');
        bytes[off++] = (byte) (year % 10 + '0');
        bytes[off++] = '-';
        bytes[off++] = (byte) (month / 10 + '0');
        bytes[off++] = (byte) (month % 10 + '0');
        bytes[off++] = '-';
        bytes[off++] = (byte) (dayOfMonth / 10 + '0');
        bytes[off++] = (byte) (dayOfMonth % 10 + '0');
    }

    public final void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second) {
        if (off + 19 >= this.bytes.length) {
            flush();
        }

        bytes[off++] = (byte) (year / 1000 + '0');
        bytes[off++] = (byte) ((year / 100) % 10 + '0');
        bytes[off++] = (byte) ((year / 10) % 10 + '0');
        bytes[off++] = (byte) (year % 10 + '0');
        bytes[off++] = '-';
        bytes[off++] = (byte) (month / 10 + '0');
        bytes[off++] = (byte) (month % 10 + '0');
        bytes[off++] = '-';
        bytes[off++] = (byte) (dayOfMonth / 10 + '0');
        bytes[off++] = (byte) (dayOfMonth % 10 + '0');
        bytes[off++] = ' ';
        bytes[off++] = (byte) (hour / 10 + '0');
        bytes[off++] = (byte) (hour % 10 + '0');
        bytes[off++] = ':';
        bytes[off++] = (byte) (minute / 10 + '0');
        bytes[off++] = (byte) (minute % 10 + '0');
        bytes[off++] = ':';
        bytes[off++] = (byte) (second / 10 + '0');
        bytes[off++] = (byte) (second % 10 + '0');
    }

    public void writeString(String value) {
        String str = value;
        byte[] bytes;
        if (JDKUtils.STRING_CODER != null
                && JDKUtils.STRING_VALUE != null
                && JDKUtils.STRING_CODER.applyAsInt(str) == JDKUtils.LATIN1) {
            bytes = JDKUtils.STRING_VALUE.apply(str);
        } else {
            bytes = str.getBytes(charset);
        }
        writeString(bytes);
    }

    public void writeInt32(int intValue) {
        if (intValue == Integer.MIN_VALUE) {
            writeRaw("-2147483648");
            return;
        }

        int size = (intValue < 0) ? IOUtils.stringSize(-intValue) + 1 : IOUtils.stringSize(intValue);

        int minCapacity = off + size;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        IOUtils.getChars(intValue, off + size, bytes);
        off += size;
    }

    public void writeDouble(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return;
        }

        int minCapacity = off + 24;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        int size = RyuDouble.toString(value, this.bytes, off);
        off += size;
    }

    public void writeFloat(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return;
        }

        int minCapacity = off + 15;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        int size = RyuFloat.toString(value, this.bytes, off);
        off += size;
    }

    public void flush() {
        try {
            out.write(bytes, 0, off);
            off = 0;
            out.flush();
        } catch (IOException e) {
            throw new JSONException("write csv error", e);
        }
    }

    public void writeString(byte[] utf8) {
        if (utf8 == null || utf8.length == 0) {
            return;
        }

        final int len = utf8.length;
        int escapeCount = 0;
        boolean comma = false;

        if (utf8[0] == '"') {
            for (int i = 0; i < len; i++) {
                byte ch = utf8[i];
                if (ch == '"') {
                    escapeCount++;
                }
            }
        } else {
            for (int i = 0; i < len; i++) {
                byte ch = utf8[i];
                if (ch == ',') {
                    comma = true;
                } else if (ch == '"') {
                    escapeCount++;
                }
            }
            if (!comma) {
                escapeCount = 0;
            }
        }

        if (escapeCount == 0) {
            writeRaw(utf8);
            return;
        }

        if (utf8[0] == '"') {
            byte[] bytes = new byte[utf8.length + 2 + escapeCount];
            int j = 0;
            bytes[0] = '"';
            for (int i = off, end = off + len; i < end; i++) {
                byte b = bytes[i];
                bytes[j++] = b;
                if (b == '"') {
                    bytes[j++] = b;
                }
            }
            bytes[j] = '"';
            writeRaw(bytes);
        } else {
            byte[] bytes = new byte[utf8.length + 2 + escapeCount];
            int j = 0;
            bytes[0] = '"';
            for (int i = off, end = off + len; i < end; i++) {
                byte b = bytes[i];
                bytes[j++] = b;
                if (b == '"') {
                    bytes[j++] = b;
                }
            }
            bytes[j] = '"';
            writeRaw(bytes);
        }
    }

    public void writeDecimal(BigDecimal value) {
        if (value == null) {
            return;
        }

        int minCapacity = off + value.precision() + 2;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        int size = IOUtils.getDecimalChars(value, bytes, off);
        off += size;
    }

    public void writeDecimal(long unscaledVal, int scale) {
        if (scale == 0) {
            writeInt64(unscaledVal);
            return;
        }

        if (unscaledVal == Long.MIN_VALUE || scale > 20) {
            writeDecimal(BigDecimal.valueOf(unscaledVal, scale));
            return;
        }

        int minCapacity = off + 24;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        int size = IOUtils.getDecimalChars(unscaledVal, scale, bytes, off);
        off += size;
    }

    public String toString() {
        if (out instanceof ByteArrayOutputStream) {
            flush();
            byte[] strBytes = ((ByteArrayOutputStream) out).toByteArray();
            return new String(strBytes, StandardCharsets.UTF_8);
        }
        return super.toString();
    }

    protected void writeRaw(byte[] strBytes) {
        if (strBytes.length + off < bytes.length) {
            System.arraycopy(strBytes, 0, bytes, off, strBytes.length);
            off += strBytes.length;
        } else {
            flush();
            if (strBytes.length >= bytes.length) {
                writeDirect(strBytes, 0, strBytes.length);
            } else {
                System.arraycopy(strBytes, 0, bytes, off, strBytes.length);
                off += strBytes.length;
            }
        }
    }

    protected void writeRaw(String str) {
        if (str == null || str.isEmpty()) {
            return;
        }

        byte[] strBytes = str.getBytes(charset);
        if (strBytes.length + off < bytes.length) {
            System.arraycopy(strBytes, 0, bytes, off, strBytes.length);
            off += strBytes.length;
        } else {
            flush();
            if (strBytes.length >= bytes.length) {
                writeDirect(strBytes, 0, strBytes.length);
            } else {
                System.arraycopy(strBytes, 0, bytes, off, strBytes.length);
                off += strBytes.length;
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (off > 0) {
            flush();
        }

        out.close();
    }

    public static CSVWriter of(OutputStream out, Feature... features) {
        return new CSVWriter(out, StandardCharsets.UTF_8, DateUtils.DEFAULT_ZONE_ID, features);
    }

    public static CSVWriter of(OutputStream out, Charset charset) {
        return of(out, charset, DateUtils.DEFAULT_ZONE_ID);
    }

    public static CSVWriter of(OutputStream out, Charset charset, ZoneId zoneId) {
        if (charset == StandardCharsets.UTF_16
                || charset == StandardCharsets.UTF_16LE
                || charset == StandardCharsets.UTF_16BE) {
            throw new UnsupportedOperationException("not support charset : " + charset);
        }

        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        return new CSVWriter(out, charset, zoneId);
    }

    public enum Feature {
        AlwaysQuoteStrings(1);

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }
    }
}
