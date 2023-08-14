package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.stream.StreamReader;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.alibaba.fastjson2.util.DateUtils.DEFAULT_ZONE_ID;

final class CSVReaderUTF8<T>
        extends CSVReader<T> {
    static final Map<Long, Function<Consumer, ByteArrayValueConsumer>> valueConsumerCreators
            = new ConcurrentHashMap<>();

    byte[] buf;
    InputStream input;
    Charset charset = StandardCharsets.UTF_8;
    ByteArrayValueConsumer valueConsumer;

    CSVReaderUTF8(Feature... features) {
        for (Feature feature : features) {
            this.features |= feature.mask;
        }
    }

    CSVReaderUTF8(byte[] bytes, int off, int len, Charset charset, Class<T> objectClass) {
        super(objectClass);
        this.buf = bytes;
        this.off = off;
        this.end = off + len;
        this.charset = charset;
    }

    CSVReaderUTF8(byte[] bytes, int off, int len, Charset charset, ByteArrayValueConsumer valueConsumer) {
        this.valueConsumer = valueConsumer;
        this.buf = bytes;
        this.off = off;
        this.end = off + len;
        this.charset = charset;
    }

    CSVReaderUTF8(byte[] bytes, int off, int len, Type[] types) {
        super(types);
        this.buf = bytes;
        this.off = off;
        this.end = off + len;
        this.types = types;
    }

    CSVReaderUTF8(byte[] bytes, int off, int len, Class<T> objectClass) {
        super(objectClass);
        this.buf = bytes;
        this.off = off;
        this.end = off + len;
    }

    CSVReaderUTF8(InputStream input, Charset charset, Type[] types) {
        super(types);
        this.charset = charset;
        this.input = input;
    }

    CSVReaderUTF8(InputStream input, Charset charset, Class<T> objectClass) {
        super(objectClass);
        this.charset = charset;
        this.input = input;
    }

    CSVReaderUTF8(InputStream input, Charset charset, ByteArrayValueConsumer valueConsumer) {
        this.charset = charset;
        this.input = input;
        this.valueConsumer = valueConsumer;
    }

    protected boolean seekLine() throws IOException {
        byte[] buf = this.buf;
        int off = this.off;
        if (buf == null) {
            if (input != null) {
                buf = this.buf = new byte[SIZE_512K];
                int cnt = input.read(buf);
                if (cnt == -1) {
                    inputEnd = true;
                    return false;
                }
                this.end = cnt;

                if (end > 3) {
                    // UTF8-BOM EF BB BF
                    if (buf[0] == -17 && buf[1] == -69 && buf[2] == -65) {
                        off = 3;
                        lineNextStart = off;
                    }
                }
            }
        }

        for (int k = 0; k < 3; ++k) {
            lineTerminated = false;

            for (int i = off; i < end; i++) {
                byte ch = buf[i];
                if (ch == '"') {
                    lineSize++;
                    if (!quote) {
                        quote = true;
                    } else {
                        int n = i + 1;
                        if (n >= end) {
                            break;
                        }
                        if (buf[n] == '"') {
                            lineSize++;
                            i++;
                        } else {
                            quote = false;
                        }
                    }
                    continue;
                }

                if (quote) {
                    lineSize++;
                    continue;
                }

                if (ch == '\n') {
                    if (lineSize > 0 || (features & Feature.IgnoreEmptyLine.mask) == 0) {
                        rowCount++;
                    }
                    lineTerminated = true;
                    lineSize = 0;
                    lineEnd = i;
                    lineStart = lineNextStart;
                    lineNextStart = off = i + 1;

                    break;
                } else if (ch == '\r') {
                    if (lineSize > 0 || (features & Feature.IgnoreEmptyLine.mask) == 0) {
                        rowCount++;
                    }

                    lineTerminated = true;
                    lineSize = 0;
                    lineEnd = i;

                    int n = i + 1;
                    if (n >= end) {
                        break;
                    }
                    if (buf[n] == '\n') {
                        i++;
                    }

                    lineStart = lineNextStart;
                    lineNextStart = off = i + 1;

                    break;
                } else {
                    lineSize++;
                }
            }

            if (!lineTerminated) {
                if (input != null && !inputEnd) {
                    int len = end - off;
                    if (off > 0) {
                        if (len > 0) {
                            System.arraycopy(buf, off, buf, 0, len);
                        }
                        lineStart = lineNextStart = 0;
                        off = 0;
                        end = len;
                        quote = false;
                    }

                    int cnt = input.read(buf, end, buf.length - end);
                    if (cnt == -1) {
                        inputEnd = true;
                        if (off == end) {
                            this.off = off;
                            return false;
                        }
                    } else {
                        end += cnt;
                        continue;
                    }
                }

                lineStart = lineNextStart;
                lineEnd = end;
                rowCount++;
                lineSize = 0;
                off = end;
            }

            lineTerminated = off == end;
            break;
        }

        this.off = off;
        return true;
    }

    Object readValue(byte[] bytes, int off, int len, Type type) {
        if (len == 0) {
            return null;
        }

        if (type == Integer.class) {
            return TypeUtils.parseInt(bytes, off, len);
        }

        if (type == Long.class) {
            return TypeUtils.parseLong(bytes, off, len);
        }

        if (type == BigDecimal.class) {
            return TypeUtils.parseBigDecimal(bytes, off, len);
        }

        if (type == Float.class) {
            return TypeUtils.parseFloat(bytes, off, len);
        }

        if (type == Double.class) {
            return TypeUtils.parseDouble(bytes, off, len);
        }

        if (type == Date.class) {
            long millis = DateUtils.parseMillis(bytes, off, len, charset, DEFAULT_ZONE_ID);
            return new Date(millis);
        }

        if (type == Boolean.class) {
            return TypeUtils.parseBoolean(bytes, off, len);
        }

        String str = new String(bytes, off, len, charset);
        return TypeUtils.cast(str, type);
    }

    public boolean isEnd() {
        return inputEnd;
    }

    public Object[] readLineValues(boolean strings) {
        try {
            if (inputEnd) {
                return null;
            }

            if (input == null) {
                if (off >= end) {
                    return null;
                }
            }

            boolean result = seekLine();

            if (!result) {
                return null;
            }
        } catch (IOException e) {
            throw new JSONException("seekLine error", e);
        }

        Object[] values = null;
        List<Object> valueList = null;
        if (columns != null) {
            if (strings) {
                values = new String[columns.size()];
            } else {
                values = new Object[columns.size()];
            }
        }

        boolean quote = false;
        int valueStart = lineStart;
        int valueSize = 0;
        int escapeCount = 0;
        int columnIndex = 0;
        for (int i = lineStart; i < lineEnd; ++i) {
            byte ch = buf[i];

            if (quote) {
                if (ch == '"') {
                    int n = i + 1;
                    if (n < lineEnd) {
                        byte c1 = buf[n];
                        if (c1 == '"') {
                            valueSize += 2;
                            escapeCount++;
                            ++i;
                            continue;
                        } else if (c1 == ',') {
                            ++i;
                            ch = c1;
                        }
                    } else if (n == lineEnd) {
                        break;
                    }
                } else {
                    valueSize++;
                    continue;
                }
            } else {
                if (ch == '"') {
                    quote = true;
                    continue;
                }
            }

            if (ch == ',') {
                Type type = types != null && columnIndex < types.length ? types[columnIndex] : null;

                Object value;
                if (quote) {
                    if (escapeCount == 0) {
                        if (type == null || type == String.class || type == Object.class) {
                            value = new String(buf, valueStart + 1, valueSize, charset);
                        } else {
                            try {
                                value = readValue(buf, valueStart + 1, valueSize, type);
                            } catch (Exception e) {
                                value = error(columnIndex, e);
                            }
                        }
                    } else {
                        byte[] bytes = new byte[valueSize - escapeCount];
                        int valueEnd = valueStart + valueSize;
                        for (int j = valueStart + 1, k = 0; j < valueEnd; ++j) {
                            byte c = buf[j];
                            bytes[k++] = c;
                            if (c == '"' && buf[j + 1] == '"') {
                                ++j;
                            }
                        }

                        if (type == null || type == String.class || type == Object.class) {
                            value = new String(bytes, 0, bytes.length, charset);
                        } else {
                            try {
                                value = readValue(bytes, 0, bytes.length, type);
                            } catch (Exception e) {
                                value = error(columnIndex, e);
                            }
                        }
                    }
                } else {
                    if (type == null || type == String.class || type == Object.class || strings) {
                        byte c0, c1;
                        if (valueSize == 1 && (c0 = buf[valueStart]) >= 0) {
                            value = TypeUtils.toString((char) c0);
                        } else if (valueSize == 2
                                && (c0 = buf[valueStart]) >= 0
                                && (c1 = buf[valueStart + 1]) >= 0
                        ) {
                            value = TypeUtils.toString((char) c0, (char) c1);
                        } else {
                            value = new String(buf, valueStart, valueSize, charset);
                        }
                    } else {
                        try {
                            value = readValue(buf, valueStart, valueSize, type);
                        } catch (Exception e) {
                            value = error(columnIndex, e);
                        }
                    }
                }

                if (values != null) {
                    if (columnIndex < values.length) {
                        values[columnIndex] = value;
                    }
                } else {
                    if (valueList == null) {
                        valueList = new ArrayList<>();
                    }
                    valueList.add(value);
                }

                quote = false;
                valueStart = i + 1;
                valueSize = 0;
                escapeCount = 0;
                columnIndex++;
                continue;
            }

            valueSize++;
        }

        if (valueSize > 0) {
            Type type = types != null && columnIndex < types.length ? types[columnIndex] : null;

            Object value;
            if (quote) {
                if (escapeCount == 0) {
                    if (type == null || type == String.class || type == Object.class) {
                        value = new String(buf, valueStart + 1, valueSize, charset);
                    } else {
                        try {
                            value = readValue(buf, valueStart + 1, valueSize, type);
                        } catch (Exception e) {
                            value = error(columnIndex, e);
                        }
                    }
                } else {
                    byte[] bytes = new byte[valueSize - escapeCount];
                    int valueEnd = lineEnd;
                    for (int j = valueStart + 1, k = 0; j < valueEnd; ++j) {
                        byte c = buf[j];
                        bytes[k++] = c;
                        if (c == '"' && buf[j + 1] == '"') {
                            ++j;
                        }
                    }

                    if (type == null || type == String.class || type == Object.class) {
                        value = new String(bytes, 0, bytes.length, charset);
                    } else {
                        try {
                            value = readValue(bytes, 0, bytes.length, type);
                        } catch (Exception e) {
                            value = error(columnIndex, e);
                        }
                    }
                }
            } else {
                if (type == null || type == String.class || type == Object.class || strings) {
                    byte c0, c1;
                    if (valueSize == 1 && (c0 = buf[valueStart]) >= 0) {
                        value = TypeUtils.toString((char) c0);
                    } else if (valueSize == 2
                            && (c0 = buf[valueStart]) >= 0
                            && (c1 = buf[valueStart + 1]) >= 0
                    ) {
                        value = TypeUtils.toString((char) c0, (char) c1);
                    } else {
                        value = new String(buf, valueStart, valueSize, charset);
                    }
                } else {
                    try {
                        value = readValue(buf, valueStart, valueSize, type);
                    } catch (Exception e) {
                        value = error(columnIndex, e);
                    }
                }
            }

            if (values != null) {
                if (columnIndex < values.length) {
                    values[columnIndex] = value;
                }
            } else {
                if (valueList == null) {
                    valueList = new ArrayList<>();
                }
                valueList.add(value);
            }
        }

        if (values == null) {
            if (valueList != null) {
                if (strings) {
                    values = new String[valueList.size()];
                } else {
                    values = new Object[valueList.size()];
                }
                valueList.toArray(values);
            }
        }

        if (input == null && off == end) {
            inputEnd = true;
        }

        return values;
    }

    @Override
    public void close() {
        if (input != null) {
            IOUtils.close(input);
        }
    }

    public void statAll() {
        ByteArrayValueConsumer consumer = (row, column, bytes, off, len, charset) -> {
            StreamReader.ColumnStat stat = getColumnStat(column);
            stat.stat(bytes, off, len, charset);
        };
        readAll(consumer, Integer.MAX_VALUE);
    }

    public void statAll(int maxRows) {
        ByteArrayValueConsumer consumer = (row, column, bytes, off, len, charset) -> {
            StreamReader.ColumnStat stat = getColumnStat(column);
            stat.stat(bytes, off, len, charset);
        };
        readAll(consumer, maxRows);
    }

    public void readAll() {
        if (valueConsumer == null) {
            throw new JSONException("unsupported operation, consumer is null");
        }

        readAll(valueConsumer, Integer.MAX_VALUE);
    }

    public void readAll(int maxRows) {
        if (valueConsumer == null) {
            throw new JSONException("unsupported operation, consumer is null");
        }

        readAll(valueConsumer, maxRows);
    }

    public void readLineObjectAll(boolean readHeader, Consumer<T> consumer) {
        if (readHeader) {
            readHeader();
        }

        if (fieldReaders == null) {
            while (true) {
                Object[] line = readLineValues(false);
                if (line == null) {
                    break;
                }
                consumer.accept((T) line);
            }
            return;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();

        // valueConsumerCreators
        if (this.fieldReaders == null) {
            if (objectClass != null) {
                ObjectReaderAdapter objectReader = (ObjectReaderAdapter) provider.getObjectReader(objectClass);
                this.fieldReaders = objectReader.getFieldReaders();
                this.objectCreator = provider.createObjectCreator(objectClass, features);
            }
        }

        Function<Consumer, ByteArrayValueConsumer> valueConsumerCreator;
        String[] strings = new String[this.fieldReaders.length + 1];
        strings[0] = objectClass.getName();
        for (int i = 0; i < this.fieldReaders.length; i++) {
            strings[i + 1] = this.fieldReaders[i].fieldName;
        }
        long fullNameHash = Fnv.hashCode64(strings);
        valueConsumerCreator = valueConsumerCreators.get(fullNameHash);
        if (valueConsumerCreator == null) {
            valueConsumerCreator = provider
                    .createValueConsumerCreator(objectClass, fieldReaders);
            if (valueConsumerCreator != null) {
                valueConsumerCreators.putIfAbsent(fullNameHash, valueConsumerCreator);
            }
        }

        ByteArrayValueConsumer bytesConsumer = null;
        if (valueConsumerCreator != null) {
            bytesConsumer = valueConsumerCreator.apply(consumer);
        }

        if (bytesConsumer == null) {
            bytesConsumer = new ByteArrayConsumerImpl(consumer);
        }

        readAll(bytesConsumer, Integer.MAX_VALUE);
    }

    class ByteArrayConsumerImpl
            implements ByteArrayValueConsumer {
        protected Object object;
        final Consumer consumer;

        public ByteArrayConsumerImpl(Consumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public final void beforeRow(int row) {
            if (objectCreator != null) {
                object = objectCreator.get();
            }
        }

        @Override
        public void accept(int row, int column, byte[] bytes, int off, int len, Charset charset) {
            if (column >= fieldReaders.length || len == 0) {
                return;
            }

            FieldReader fieldReader = fieldReaders[column];
            Object fieldValue = readValue(bytes, off, len, fieldReader.fieldType);
            fieldReader.accept(object, fieldValue);
        }

        @Override
        public final void afterRow(int row) {
            consumer.accept(object);
            object = null;
        }
    }

    private void readAll(ByteArrayValueConsumer consumer, int maxRows) {
        consumer.start();

        for (int r = 0; r < maxRows || maxRows < 0; ++r) {
            try {
                if (inputEnd) {
                    break;
                }

                if (input == null) {
                    if (off >= end) {
                        break;
                    }
                }

                boolean result = seekLine();

                if (!result) {
                    break;
                }
            } catch (IOException e) {
                throw new JSONException("seekLine error", e);
            }

            consumer.beforeRow(rowCount);

            boolean quote = false;
            int valueStart = lineStart;
            int valueSize = 0;
            int escapeCount = 0;
            int columnIndex = 0;
            for (int i = lineStart; i < lineEnd; ++i) {
                byte ch = buf[i];

                if (quote) {
                    if (ch == '"') {
                        int n = i + 1;
                        if (n < lineEnd) {
                            byte c1 = buf[n];
                            if (c1 == '"') {
                                valueSize += 2;
                                escapeCount++;
                                ++i;
                                continue;
                            } else if (c1 == ',') {
                                ++i;
                                ch = c1;
                            }
                        } else if (n == lineEnd) {
                            break;
                        }
                    } else {
                        valueSize++;
                        continue;
                    }
                } else {
                    if (ch == '"') {
                        quote = true;
                        continue;
                    }
                }

                if (ch == ',') {
                    byte[] columnBuf = buf;
                    int columnStart = 0;
                    int columnSize = valueSize;
                    if (quote) {
                        if (escapeCount == 0) {
                            columnStart = valueStart + 1;
                        } else {
                            byte[] bytes = new byte[valueSize - escapeCount];
                            int valueEnd = valueStart + valueSize;
                            for (int j = valueStart + 1, k = 0; j < valueEnd; ++j) {
                                byte c = buf[j];
                                bytes[k++] = c;
                                if (c == '"' && buf[j + 1] == '"') {
                                    ++j;
                                }
                            }

                            columnBuf = bytes;
                            columnSize = bytes.length;
                        }
                    } else {
                        columnStart = valueStart;
                    }
                    consumer.accept(rowCount, columnIndex, columnBuf, columnStart, columnSize, charset);

                    quote = false;
                    valueStart = i + 1;
                    valueSize = 0;
                    escapeCount = 0;
                    columnIndex++;
                    continue;
                }

                valueSize++;
            }

            if (valueSize > 0) {
                byte[] columnBuf = buf;
                int columnStart = 0;
                int columnSize = valueSize;
                if (quote) {
                    if (escapeCount == 0) {
                        columnStart = valueStart + 1;
                    } else {
                        byte[] bytes = new byte[valueSize - escapeCount];
                        int valueEnd = lineEnd;
                        for (int j = valueStart + 1, k = 0; j < valueEnd; ++j) {
                            byte c = buf[j];
                            bytes[k++] = c;
                            if (c == '"' && buf[j + 1] == '"') {
                                ++j;
                            }
                        }

                        columnBuf = bytes;
                        columnSize = bytes.length;
                    }
                } else {
                    columnStart = valueStart;
                }
                consumer.accept(rowCount, columnIndex, columnBuf, columnStart, columnSize, charset);
            }
            consumer.afterRow(rowCount);
        }
        consumer.end();
    }
}
