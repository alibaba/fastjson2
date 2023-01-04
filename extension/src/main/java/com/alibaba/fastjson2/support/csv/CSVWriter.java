package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public abstract class CSVWriter
        implements Closeable, Flushable {
    private long features;

    public CSVWriter(Feature... features) {
        for (Feature feature : features) {
            this.features |= feature.mask;
        }
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

    public abstract void writeRow(Object... values);

    public abstract void writeString(String str);

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

    protected abstract void writeRaw(char ch);

    protected abstract void writeRaw(String str);

    public static CSVWriter of(OutputStream out, Feature... features) {
        return new CSVWriterUTF8(out, StandardCharsets.UTF_8, features);
    }

    public static CSVWriter of(OutputStream out, Charset charset) {
        if (charset == StandardCharsets.UTF_16
                || charset == StandardCharsets.UTF_16LE
                || charset == StandardCharsets.UTF_16BE) {
            return new CSVWriterUTF16(
                    new OutputStreamWriter(out, charset)
            );
        }

        return new CSVWriterUTF8(out, charset);
    }

    public enum Feature {
        AlwaysQuoteStrings(1);

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }
    }
}
