package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.reader.ByteArrayValueConsumer;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;
import com.alibaba.fastjson2.stream.StreamReader;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

final class CSVReaderUTF8<T>
        extends CSVReader<T> {
    byte[] buf;
    InputStream input;
    Charset charset = StandardCharsets.UTF_8;
    BiFunction<Consumer, int[], ByteArrayValueConsumer> valueConsumerCreator;

    CSVReaderUTF8(Feature... features) {
        for (Feature feature : features) {
            this.features |= feature.mask;
        }
    }

    CSVReaderUTF8(byte[] bytes, int off, int len, ObjectReaderAdapter objectReader) {
        super(objectReader);
        this.buf = bytes;
        this.off = off;
        this.end = off + len;
    }

    CSVReaderUTF8(byte[] bytes, int off, int len, Type[] types) {
        super(types);
        this.buf = bytes;
        this.off = off;
        this.end = off + len;
        this.types = types;
    }

    CSVReaderUTF8(InputStream input, Charset charset, Type[] types) {
        super(types);
        this.charset = charset;
        this.input = input;
    }

    CSVReaderUTF8(
            InputStream input,
            Charset charset,
            ObjectReaderAdapter objectReader,
            BiFunction<Consumer, int[], ByteArrayValueConsumer> valueConsumerCreator
    ) {
        super(objectReader);
        this.charset = charset;
        this.input = input;
        this.valueConsumerCreator = valueConsumerCreator;
    }

    protected boolean seekLine() throws IOException {
        if (buf == null) {
            if (input != null) {
                buf = new byte[SIZE_512K];
                int cnt = input.read(buf);
                if (cnt == -1) {
                    inputEnd = true;
                    return false;
                }
                this.end = cnt;

                if (end > 3) {
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
                if (i + 4 < end) {
                    byte b0 = buf[i];
                    byte b1 = buf[i + 1];
                    byte b2 = buf[i + 2];
                    byte b3 = buf[i + 3];
                    if (b0 > '"' && b1 > '"' && b2 > '"' && b3 > '"') {
                        lineSize += 4;
                        i += 3;
                        continue;
                    }
                }

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

        if (type == Double.class) {
            return TypeUtils.parseDouble(bytes, off, len);
        }

        if (type == Date.class) {
            long millis = DateUtils.parseMillis(bytes, off, len, charset);
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
                        value = new String(buf, valueStart, valueSize, charset);
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
                    value = new String(buf, valueStart, valueSize, charset);
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
        readAll(consumer);
    }

    public void readLineObjectAll(boolean readHeader, Consumer<T> consumer) {
        if (readHeader) {
            readHeader();
        }

        ByteArrayValueConsumer bytesConsumer = null;
        if (valueConsumerCreator != null) {
            bytesConsumer = valueConsumerCreator.apply(consumer, mapping);
        }

        if (bytesConsumer == null) {
            bytesConsumer = new ByteArrayConsumerImpl(consumer);
        }

        readAll(bytesConsumer);
    }

    class ByteArrayConsumerImpl<T>
            implements ByteArrayValueConsumer {
        protected T object;
        final Consumer<T> consumer;

        public ByteArrayConsumerImpl(Consumer<T> consumer) {
            this.consumer = consumer;
        }

        @Override
        public final void beforeRow(int row) {
            object = (T) objectReader.createInstance();
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

    public void readAll(ByteArrayValueConsumer consumer) {
        while (true) {
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
                    if (quote) {
                        if (escapeCount == 0) {
//                            value = new String(buf, valueStart + 1, valueSize, charset);
                            consumer.accept(rowCount, columnIndex, buf, valueStart + 1, valueSize, charset);
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

                            consumer.accept(rowCount, columnIndex, bytes, 0, bytes.length, charset);
                        }
                    } else {
                        consumer.accept(rowCount, columnIndex, buf, valueStart, valueSize, charset);
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
                if (quote) {
                    if (escapeCount == 0) {
//                        value = new String(buf, valueStart + 1, valueSize, charset);
                        consumer.accept(rowCount, columnIndex, buf, valueStart + 1, valueSize, charset);
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

//                        value = new String(bytes, 0, bytes.length, charset);
                        consumer.accept(rowCount, columnIndex, bytes, 0, bytes.length, charset);
                    }
                } else {
//                    value = new String(buf, valueStart, valueSize, charset);
                    consumer.accept(rowCount, columnIndex, buf, valueStart, valueSize, charset);
                }
            }
            consumer.afterRow(rowCount);
        }
    }
}
