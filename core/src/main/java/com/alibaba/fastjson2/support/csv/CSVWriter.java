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

/**
 * An abstract base class for writing CSV (Comma-Separated Values) data.
 *
 * <p>This class provides methods for writing various data types to CSV format,
 * handling proper escaping and formatting according to CSV standards.
 *
 * <p>Two implementations are provided:
 * <ul>
 *   <li>{@link CSVWriterUTF8} - for UTF-8 encoded output</li>
 *   <li>{@link CSVWriterUTF16} - for UTF-16 encoded output</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // Create a CSV writer to a file
 * try (CSVWriter writer = CSVWriter.of(new File("data.csv"))) {
 *     // Write a header row
 *     writer.writeLine("Name", "Age", "City");
 *
 *     // Write data rows
 *     writer.writeLine("John Doe", 30, "New York");
 *     writer.writeLine("Jane Smith", 25, "Los Angeles");
 * }
 *
 * // Create a CSV writer to a string
 * try (CSVWriter writer = CSVWriter.of(new StringWriter())) {
 *     writer.writeLine("ID", "Value");
 *     writer.writeLine(123, "Some value");
 *     String csvContent = writer.toString();
 * }
 * </pre>
 */
public abstract class CSVWriter
        implements Closeable, Flushable {
    private long features;

    final ZoneId zoneId;

    int off;

    /**
     * Constructs a CSVWriter with the specified zone ID and features.
     *
     * @param zoneId the time zone ID to use for date/time formatting
     * @param features optional features to enable for this writer
     */
    CSVWriter(ZoneId zoneId, Feature... features) {
        for (Feature feature : features) {
            this.features |= feature.mask;
        }

        this.zoneId = zoneId;
    }

    /**
     * Creates a new CSVWriter with default settings using an in-memory ByteArrayOutputStream.
     *
     * @return a new CSVWriter instance
     */
    public static CSVWriter of() {
        return of(new ByteArrayOutputStream());
    }

    /**
     * Creates a new CSVWriter that writes to the specified file using UTF-8 encoding.
     *
     * @param file the file to write to
     * @return a new CSVWriter instance
     * @throws FileNotFoundException if the file cannot be opened for writing
     */
    public static CSVWriter of(File file) throws FileNotFoundException {
        return of(new FileOutputStream(file), StandardCharsets.UTF_8);
    }

    /**
     * Creates a new CSVWriter that writes to the specified file using the specified charset.
     *
     * @param file the file to write to
     * @param charset the charset to use for encoding
     * @return a new CSVWriter instance
     * @throws FileNotFoundException if the file cannot be opened for writing
     */
    public static CSVWriter of(File file, Charset charset) throws FileNotFoundException {
        return of(new FileOutputStream(file), charset);
    }

    /**
     * Writes an object as a CSV line by extracting its fields.
     *
     * <p>If the object is null, an empty line is written.
     * If the object has multiple fields, each field value is written as a separate column.
     * If the object has a single value field, that value is written as a single column.
     *
     * @param object the object to write as a CSV line
     */
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

    /**
     * Writes a Date value to the CSV.
     *
     * <p>If the date is null, nothing is written.
     *
     * @param date the date to write
     */
    public final void writeDate(Date date) {
        if (date == null) {
            return;
        }
        long millis = date.getTime();
        writeDate(millis);
    }

    /**
     * Writes an Instant value to the CSV.
     *
     * <p>If the instant is null, nothing is written.
     *
     * @param instant the instant to write
     */
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

    /**
     * Writes a LocalDate value to the CSV in ISO format (yyyy-MM-dd).
     *
     * <p>If the date is null, nothing is written.
     *
     * @param date the local date to write
     */
    public void writeLocalDate(LocalDate date) {
        if (date == null) {
            return;
        }
        String str = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
        writeRaw(str);
    }

    /**
     * Writes a LocalDateTime value to the CSV.
     *
     * <p>The format used is "yyyy-MM-dd HH:mm:ss" with optional fractional seconds.
     *
     * @param instant the local datetime to write
     */
    public abstract void writeLocalDateTime(LocalDateTime instant);

//    protected abstract void writeDirect(byte[] bytes, int off, int len);

    /**
     * Writes a CSV line with the specified number of columns, using a function to provide values.
     *
     * @param columnCount the number of columns to write
     * @param function a function that provides the value for each column index
     */
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

    /**
     * Writes a CSV line with the specified list of values.
     *
     * @param values the list of values to write as columns
     */
    public final void writeLine(List values) {
        for (int i = 0; i < values.size(); i++) {
            if (i != 0) {
                writeComma();
            }

            writeValue(values.get(i));
        }

        writeLine();
    }

    /**
     * Writes a CSV line with the specified values.
     *
     * @param values the values to write as columns
     */
    public final void writeLine(Object... values) {
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                writeComma();
            }

            writeValue(values[i]);
        }

        writeLine();
    }

    /**
     * Writes a comma separator to the CSV.
     */
    public abstract void writeComma();

    /**
     * Writes a quote character to the CSV.
     */
    protected abstract void writeQuote();

    /**
     * Writes a line terminator to the CSV.
     */
    public abstract void writeLine();

    /**
     * Writes a value to the CSV, automatically determining the appropriate method based on the value's type.
     *
     * <p>If the value is null, nothing is written.
     *
     * @param value the value to write
     */
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
            this.writeInt32((Integer) value);
        } else if (value instanceof Long) {
            this.writeInt64((Long) value);
        } else if (value instanceof String) {
            writeString((String) value);
        } else if (value instanceof Boolean) {
            boolean booleanValue = (Boolean) value;
            writeBoolean(booleanValue);
        } else if (value instanceof Float) {
            float floatValue = (Float) value;
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

    /**
     * Writes a BigInteger value to the CSV.
     *
     * <p>If the value is null, nothing is written.
     *
     * @param value the BigInteger value to write
     */
    public void writeBigInteger(BigInteger value) {
        if (value == null) {
            return;
        }

        String str = value.toString();
        writeRaw(str);
    }

    /**
     * Writes a boolean value to the CSV.
     *
     * @param booleanValue the boolean value to write
     */
    public abstract void writeBoolean(boolean booleanValue);

    /**
     * Writes a long integer value to the CSV.
     *
     * @param longValue the long value to write
     */
    public abstract void writeInt64(long longValue);

    /**
     * Writes a date represented as milliseconds since the epoch to the CSV.
     *
     * @param millis the milliseconds since the epoch
     */
    public final void writeDate(long millis) {
        ZoneId zoneId = this.zoneId;

        final long SECONDS_PER_DAY = 60 * 60 * 24;
        long epochSecond = Math.floorDiv(millis, 1000L);
        int offsetTotalSeconds;
        if (zoneId == DateUtils.SHANGHAI_ZONE_ID || zoneId.getRules() == DateUtils.SHANGHAI_ZONE_RULES) {
            offsetTotalSeconds = DateUtils.getShanghaiZoneOffsetTotalSeconds(epochSecond);
        } else {
            Instant instant = Instant.ofEpochMilli(millis);
            offsetTotalSeconds = zoneId.getRules().getOffset(instant).getTotalSeconds();
        }

        long localSecond = epochSecond + offsetTotalSeconds;
        long localEpochDay = Math.floorDiv(localSecond, SECONDS_PER_DAY);
        int secsOfDay = (int) Math.floorMod(localSecond, SECONDS_PER_DAY);
        int year, month, dayOfMonth;
        {
            final int DAYS_PER_CYCLE = 146097;
            final long DAYS_0000_TO_1970 = (DAYS_PER_CYCLE * 5L) - (30L * 365L + 7L);

            long zeroDay = localEpochDay + DAYS_0000_TO_1970;
            // find the march-based year
            zeroDay -= 60;  // adjust to 0000-03-01 so leap day is at end of four-year cycle
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

    /**
     * Writes a date in YYYY-MM-DD format to the CSV.
     *
     * @param year the year
     * @param month the month (1-12)
     * @param dayOfMonth the day of month (1-31)
     */
    public abstract void writeDateYYYMMDD10(int year, int month, int dayOfMonth);

    /**
     * Writes a date and time in YYYY-MM-DD HH:MM:SS format to the CSV.
     *
     * @param year the year
     * @param month the month (1-12)
     * @param dayOfMonth the day of month (1-31)
     * @param hour the hour (0-23)
     * @param minute the minute (0-59)
     * @param second the second (0-59)
     */
    public abstract void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second);

    /**
     * Writes a string value to the CSV.
     *
     * <p>The string will be properly escaped if necessary according to CSV standards.
     *
     * @param value the string value to write
     */
    public abstract void writeString(String value);

    /**
     * Writes an integer value to the CSV.
     *
     * @param intValue the integer value to write
     */
    public abstract void writeInt32(int intValue);

    /**
     * Writes a double value to the CSV.
     *
     * @param value the double value to write
     */
    public abstract void writeDouble(double value);

    /**
     * Writes a float value to the CSV.
     *
     * @param value the float value to write
     */
    public abstract void writeFloat(float value);

    /**
     * Flushes any buffered data to the underlying output stream.
     */
    public abstract void flush();

    /**
     * Writes a byte array as a string value to the CSV.
     *
     * @param utf8 the UTF-8 encoded byte array to write
     */
    public abstract void writeString(byte[] utf8);

    /**
     * Writes a BigDecimal value to the CSV.
     *
     * @param value the BigDecimal value to write
     */
    public abstract void writeDecimal(BigDecimal value);

    /**
     * Writes a decimal value represented by an unscaled value and scale to the CSV.
     *
     * @param unscaledVal the unscaled value
     * @param scale the scale (number of decimal places)
     */
    public abstract void writeDecimal(long unscaledVal, int scale);

    /**
     * Writes a raw string to the CSV without any escaping.
     *
     * @param str the string to write
     */
    protected abstract void writeRaw(String str);

    /**
     * Closes the CSV writer and releases any resources associated with it.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public abstract void close() throws IOException;

    /**
     * Creates a new CSVWriter that writes to the specified OutputStream using UTF-8 encoding.
     *
     * @param out the OutputStream to write to
     * @param features optional features to enable for this writer
     * @return a new CSVWriter instance
     */
    public static CSVWriter of(OutputStream out, Feature... features) {
        return new CSVWriterUTF8(out, StandardCharsets.UTF_8, DEFAULT_ZONE_ID, features);
    }

    /**
     * Creates a new CSVWriter that writes to the specified OutputStream using the specified charset.
     *
     * @param out the OutputStream to write to
     * @param charset the charset to use for encoding
     * @return a new CSVWriter instance
     */
    public static CSVWriter of(OutputStream out, Charset charset) {
        return of(out, charset, DEFAULT_ZONE_ID);
    }

    /**
     * Creates a new CSVWriter that writes to the specified OutputStream using the specified charset and zone ID.
     *
     * @param out the OutputStream to write to
     * @param charset the charset to use for encoding
     * @param zoneId the time zone ID to use for date/time formatting
     * @return a new CSVWriter instance
     */
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

    /**
     * Creates a new CSVWriter that writes to the specified Writer.
     *
     * @param out the Writer to write to
     * @return a new CSVWriter instance
     */
    public static CSVWriter of(Writer out) {
        return new CSVWriterUTF16(out, DEFAULT_ZONE_ID);
    }

    /**
     * Creates a new CSVWriter that writes to the specified Writer using the specified zone ID.
     *
     * @param out the Writer to write to
     * @param zoneId the time zone ID to use for date/time formatting
     * @return a new CSVWriter instance
     */
    public static CSVWriter of(Writer out, ZoneId zoneId) {
        return new CSVWriterUTF16(out, zoneId);
    }

    /**
     * Configuration features for CSV writing.
     */
    public enum Feature {
        /**
         * Always quote string values, even if they don't contain special characters.
         */
        AlwaysQuoteStrings(1);

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }
    }
}
