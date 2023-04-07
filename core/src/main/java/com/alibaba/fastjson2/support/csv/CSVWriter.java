package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.RyuDouble;
import com.alibaba.fastjson2.util.RyuFloat;
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

public class CSVWriter
        implements Closeable, Flushable {
    private long features;

    static final byte[] BYTES_TRUE = "true".getBytes();
    static final byte[] BYTES_FALSE = "false".getBytes();

    final OutputStream out;
    final Charset charset;

    byte[] bytes;
    int off;

    public CSVWriter(OutputStream out, Charset charset, Feature... features) {
        for (Feature feature : features) {
            this.features |= feature.mask;
        }

        this.out = out;
        this.charset = charset;
        this.bytes = new byte[1024 * 64];
    }

    public static CSVWriter of() {
        return of(new ByteArrayOutputStream());
    }

    public static CSVWriter of(File file) throws FileNotFoundException {
        return of(new FileOutputStream(file), StandardCharsets.UTF_8);
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

    public void writeDate(Date date) {
        if (date == null) {
            return;
        }
        long millis = date.getTime();
        writeInstant(Instant.ofEpochMilli(millis));
    }

    public void writeInstant(Instant instant) {
        if (instant == null) {
            return;
        }

        ZonedDateTime zdt = instant.atZone(ZoneOffset.UTC);
        String str = DateTimeFormatter.ISO_ZONED_DATE_TIME.format(zdt);
        if ((features & Feature.AlwaysQuoteStrings.mask) != 0) {
            writeRaw('"');
            writeRaw(str);
            writeRaw('"');
        } else {
            writeRaw(str);
        }
    }

    public void writeDate(LocalDate date) {
        if (date == null) {
            return;
        }
        String str = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
        writeRaw(str);
    }

    public void writeDateTime(LocalDateTime instant) {
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

    public void writeRow(Object... values) {
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                if (off + 1 >= bytes.length) {
                    flush();
                }
                bytes[off++] = ',';
            }

            Object value = values[i];
            if (value == null) {
                continue;
            }

            if (value instanceof Optional) {
                Optional optional = (Optional) value;
                if (!optional.isPresent()) {
                    continue;
                }
                value = optional.get();
            }

            if (value instanceof Integer) {
                int intValue = ((Integer) value).intValue();
                if (intValue == Integer.MIN_VALUE) {
                    writeRaw("-2147483648");
                    continue;
                }

                int size = (intValue < 0) ? IOUtils.stringSize(-intValue) + 1 : IOUtils.stringSize(intValue);

                int minCapacity = off + size;
                if (minCapacity - this.bytes.length > 0) {
                    flush();
                }

                IOUtils.getChars(intValue, off + size, bytes);
                off += size;
            } else if (value instanceof Long) {
                long longValue = ((Long) value).longValue();
                if (longValue == Long.MIN_VALUE) {
                    writeRaw("-9223372036854775808");
                    continue;
                }

                int size = (longValue < 0) ? IOUtils.stringSize(-longValue) + 1 : IOUtils.stringSize(longValue);

                int minCapacity = off + size;
                if (minCapacity - this.bytes.length > 0) {
                    flush();
                }

                IOUtils.getChars(longValue, off + size, bytes);
                off += size;
            } else if (value instanceof String) {
                String str = (String) value;
                byte[] bytes;
                if (JDKUtils.STRING_CODER != null
                        && JDKUtils.STRING_VALUE != null
                        && JDKUtils.STRING_CODER.applyAsInt(str) == JDKUtils.LATIN1) {
                    bytes = JDKUtils.STRING_VALUE.apply(str);
                } else {
                    bytes = str.getBytes(charset);
                }
                writeString(bytes);
            } else if (value instanceof Boolean) {
                boolean booleanValue = ((Boolean) value).booleanValue();
                byte[] valueBytes = booleanValue ? BYTES_TRUE : BYTES_FALSE;
                writeRaw(valueBytes);
            } else if (value instanceof Float) {
                float floatValue = ((Float) value).floatValue();
                if (Float.isNaN(floatValue) || Float.isInfinite(floatValue)) {
                    continue;
                }

                if (off + 15 > this.bytes.length) {
                    flush();
                }

                int size = RyuFloat.toString(floatValue, bytes, off);
                off += size;
            } else if (value instanceof Double) {
                double doubleValue = ((Double) value).doubleValue();
                if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
                    continue;
                }

                if (off + 24 > this.bytes.length) {
                    flush();
                }

                int size = RyuDouble.toString(doubleValue, bytes, off);
                off += size;
            } else if (value instanceof Short) {
                int intValue = ((Short) value).intValue();
                int size = (intValue < 0) ? IOUtils.stringSize(-intValue) + 1 : IOUtils.stringSize(intValue);

                int minCapacity = off + size;
                if (minCapacity - this.bytes.length > 0) {
                    flush();
                }

                IOUtils.getChars(intValue, off + size, bytes);
                off += size;
            } else if (value instanceof Byte) {
                int intValue = ((Byte) value).intValue();
                int size = (intValue < 0) ? IOUtils.stringSize(-intValue) + 1 : IOUtils.stringSize(intValue);

                int minCapacity = off + size;
                if (minCapacity - this.bytes.length > 0) {
                    flush();
                }

                IOUtils.getChars(intValue, off + size, bytes);
                off += size;
            } else if (value instanceof BigDecimal) {
                String str = value.toString();
                byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);
                writeRaw(bytes);
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
                byte[] bytes = str.getBytes(charset);
                writeString(bytes);
            }
        }

        if (off + 1 >= bytes.length) {
            flush();
        }
        bytes[off++] = '\n';
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

    private void writeString(byte[] utf8Bytes) {
        if (utf8Bytes == null || utf8Bytes.length == 0) {
            return;
        }

        final int len = utf8Bytes.length;
        int escapeCount = 0;
        boolean comma = false;

        if (utf8Bytes[0] == '"') {
            for (int i = 0; i < len; i++) {
                byte ch = utf8Bytes[i];
                if (ch == '"') {
                    escapeCount++;
                }
            }
        } else {
            for (int i = 0; i < len; i++) {
                byte ch = utf8Bytes[i];
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
            writeRaw(utf8Bytes);
            return;
        }

        if (utf8Bytes[0] == '"') {
            byte[] bytes = new byte[utf8Bytes.length + 2 + escapeCount];
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
            byte[] bytes = new byte[utf8Bytes.length + 2 + escapeCount];
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

        writeRaw(value.toString());
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

    protected void writeRaw(char ch) {
        if (ch < 0 || ch > 127) {
            throw new JSONException("unsupported operation");
        }

        if (off + 1 == bytes.length) {
            flush();
        }
        bytes[off++] = (byte) ch;
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
        return new CSVWriter(out, StandardCharsets.UTF_8, features);
    }

    public static CSVWriter of(OutputStream out, Charset charset) {
        if (charset == StandardCharsets.UTF_16
                || charset == StandardCharsets.UTF_16LE
                || charset == StandardCharsets.UTF_16BE) {
            throw new UnsupportedOperationException("not support charset : " + charset);
        }

        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        return new CSVWriter(out, charset);
    }

    public enum Feature {
        AlwaysQuoteStrings(1);

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }
    }
}
