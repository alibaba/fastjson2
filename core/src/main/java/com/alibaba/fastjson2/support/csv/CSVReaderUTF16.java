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
import java.io.Reader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.alibaba.fastjson2.util.DateUtils.DEFAULT_ZONE_ID;

final class CSVReaderUTF16<T>
        extends CSVReader<T> {
    static final Map<Long, Function<Consumer, CharArrayValueConsumer>> valueConsumerCreators
            = new ConcurrentHashMap<>();
    CharArrayValueConsumer valueConsumer;

    char[] buf;
    Reader input;

    CSVReaderUTF16(Feature... features) {
        for (Feature feature : features) {
            this.features |= feature.mask;
        }
    }

    CSVReaderUTF16(Reader input, Class<T> objectClass) {
        super(objectClass);
        this.input = input;
    }

    CSVReaderUTF16(Reader input, CharArrayValueConsumer valueConsumer) {
        this.valueConsumer = valueConsumer;
        this.input = input;
    }

    CSVReaderUTF16(Reader input, Type[] types) {
        super(types);
        this.input = input;
    }

    CSVReaderUTF16(char[] bytes, int off, int len, Class<T> objectClass) {
        super(objectClass);
        this.buf = bytes;
        this.off = off;
        this.end = off + len;
    }

    CSVReaderUTF16(char[] bytes, int off, int len, CharArrayValueConsumer valueConsumer) {
        this.valueConsumer = valueConsumer;
        this.buf = bytes;
        this.off = off;
        this.end = off + len;
    }

    CSVReaderUTF16(
            char[] bytes,
            int off,
            int len,
            Type[] types
    ) {
        super(types);
        this.buf = bytes;
        this.off = off;
        this.end = off + len;
    }

    protected boolean seekLine() throws IOException {
        if (buf == null) {
            if (input != null) {
                buf = new char[SIZE_512K];
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
                    char b0 = buf[i];
                    char b1 = buf[i + 1];
                    char b2 = buf[i + 2];
                    char b3 = buf[i + 3];
                    if (b0 > '"' && b1 > '"' && b2 > '"' && b3 > '"') {
                        lineSize += 4;
                        i += 3;
                        continue;
                    }
                }

                char ch = buf[i];
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

    Object readValue(char[] chars, int off, int len, Type type) {
        if (len == 0) {
            return null;
        }

        if (type == Integer.class) {
            return TypeUtils.parseInt(chars, off, len);
        }

        if (type == Long.class) {
            return TypeUtils.parseLong(chars, off, len);
        }

        if (type == BigDecimal.class) {
            return TypeUtils.parseBigDecimal(chars, off, len);
        }

        if (type == Float.class) {
            if (len == 0) {
                return null;
            }
            return TypeUtils.parseFloat(chars, off, len);
        }

        if (type == Double.class) {
            if (len == 0) {
                return null;
            }
            return TypeUtils.parseDouble(chars, off, len);
        }

        if (type == Date.class) {
            long millis = DateUtils.parseMillis(chars, off, len, DEFAULT_ZONE_ID);
            return new Date(millis);
        }

        if (type == Boolean.class) {
            return TypeUtils.parseBoolean(chars, off, len);
        }

        String str = new String(chars, off, len);
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
            char ch = buf[i];

            if (quote) {
                if (ch == '"') {
                    int n = i + 1;
                    if (n < lineEnd) {
                        char c1 = buf[n];
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
                        if (type == null || type == String.class || type == Object.class || strings) {
                            value = new String(buf, valueStart + 1, valueSize);
                        } else {
                            try {
                                value = readValue(buf, valueStart + 1, valueSize, type);
                            } catch (Exception e) {
                                value = error(columnIndex, e);
                            }
                        }
                    } else {
                        char[] chars = new char[valueSize - escapeCount];
                        int valueEnd = valueStart + valueSize;
                        for (int j = valueStart + 1, k = 0; j < valueEnd; ++j) {
                            char c = buf[j];
                            chars[k++] = c;
                            if (c == '"' && buf[j + 1] == '"') {
                                ++j;
                            }
                        }

                        if (type == null || type == String.class || type == Object.class || strings) {
                            value = new String(chars);
                        } else {
                            try {
                                value = readValue(chars, 0, chars.length, type);
                            } catch (Exception e) {
                                value = error(columnIndex, e);
                            }
                        }
                    }
                } else {
                    if (type == null || type == String.class || type == Object.class || strings) {
                        if (valueSize == 1) {
                            value = TypeUtils.toString(buf[valueStart]);
                        } else if (valueSize == 2) {
                            value = TypeUtils.toString(buf[valueStart], buf[valueStart + 1]);
                        } else {
                            value = new String(buf, valueStart, valueSize);
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
                    if (type == null || type == String.class || type == Object.class || strings) {
                        value = new String(buf, valueStart + 1, valueSize);
                    } else {
                        value = readValue(buf, valueStart + 1, valueSize, type);
                    }
                } else {
                    char[] chars = new char[valueSize - escapeCount];
                    int valueEnd = lineEnd;
                    for (int j = valueStart + 1, k = 0; j < valueEnd; ++j) {
                        char c = buf[j];
                        chars[k++] = c;
                        if (c == '"' && buf[j + 1] == '"') {
                            ++j;
                        }
                    }

                    if (type == null || type == String.class || type == Object.class || strings) {
                        value = new String(chars);
                    } else {
                        try {
                            value = readValue(chars, 0, chars.length, type);
                        } catch (Exception e) {
                            value = error(columnIndex, e);
                        }
                    }
                }
            } else {
                if (type == null || type == String.class || type == Object.class || strings) {
                    char c0, c1;
                    if (valueSize == 1) {
                        value = TypeUtils.toString(buf[valueStart]);
                    } else if (valueSize == 2) {
                        value = TypeUtils.toString(buf[valueStart], buf[valueStart + 1]);
                    } else {
                        value = new String(buf, valueStart, valueSize);
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
        CharArrayValueConsumer consumer = (row, column, bytes, off, len) -> {
            StreamReader.ColumnStat stat = getColumnStat(column);
            stat.stat(bytes, off, len);
        };
        readAll(consumer, Integer.MAX_VALUE);
    }

    public void statAll(int maxRows) {
        CharArrayValueConsumer consumer = (row, column, bytes, off, len) -> {
            StreamReader.ColumnStat stat = getColumnStat(column);
            stat.stat(bytes, off, len);
        };
        readAll(consumer, maxRows);
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

        Function<Consumer, CharArrayValueConsumer> valueConsumerCreator;
        String[] strings = new String[this.fieldReaders.length + 1];
        strings[0] = objectClass.getName();
        for (int i = 0; i < this.fieldReaders.length; i++) {
            strings[i + 1] = this.fieldReaders[i].fieldName;
        }
        long fullNameHash = Fnv.hashCode64(strings);
        valueConsumerCreator = valueConsumerCreators.get(fullNameHash);
        if (valueConsumerCreator == null) {
            valueConsumerCreator = provider
                    .createCharArrayValueConsumerCreator(objectClass, fieldReaders);
            if (valueConsumerCreator != null) {
                valueConsumerCreators.putIfAbsent(fullNameHash, valueConsumerCreator);
            }
        }

        CharArrayValueConsumer bytesConsumer = null;
        if (valueConsumerCreator != null) {
            bytesConsumer = valueConsumerCreator.apply(consumer);
        }

        if (bytesConsumer == null) {
            bytesConsumer = new CharArrayConsumerImpl(consumer);
        }

        readAll(bytesConsumer, Integer.MAX_VALUE);
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

    public void readAll(CharArrayValueConsumer<T> consumer, int maxRows) {
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
                char ch = buf[i];

                if (quote) {
                    if (ch == '"') {
                        int n = i + 1;
                        if (n < lineEnd) {
                            char c1 = buf[n];
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
                            consumer.accept(rowCount, columnIndex, buf, valueStart + 1, valueSize);
                        } else {
                            char[] bytes = new char[valueSize - escapeCount];
                            int valueEnd = valueStart + valueSize;
                            for (int j = valueStart + 1, k = 0; j < valueEnd; ++j) {
                                char c = buf[j];
                                bytes[k++] = c;
                                if (c == '"' && buf[j + 1] == '"') {
                                    ++j;
                                }
                            }

                            consumer.accept(rowCount, columnIndex, bytes, 0, bytes.length);
                        }
                    } else {
                        consumer.accept(rowCount, columnIndex, buf, valueStart, valueSize);
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
                        consumer.accept(rowCount, columnIndex, buf, valueStart + 1, valueSize);
                    } else {
                        char[] bytes = new char[valueSize - escapeCount];
                        int valueEnd = lineEnd;
                        for (int j = valueStart + 1, k = 0; j < valueEnd; ++j) {
                            char c = buf[j];
                            bytes[k++] = c;
                            if (c == '"' && buf[j + 1] == '"') {
                                ++j;
                            }
                        }

//                        value = new String(bytes, 0, bytes.length, charset);
                        consumer.accept(rowCount, columnIndex, bytes, 0, bytes.length);
                    }
                } else {
//                    value = new String(buf, valueStart, valueSize, charset);
                    consumer.accept(rowCount, columnIndex, buf, valueStart, valueSize);
                }
            }
            consumer.afterRow(rowCount);
        }
        consumer.end();
    }

    class CharArrayConsumerImpl<T>
            implements CharArrayValueConsumer {
        protected T object;
        final Consumer<T> consumer;

        public CharArrayConsumerImpl(Consumer<T> consumer) {
            this.consumer = consumer;
        }

        @Override
        public final void beforeRow(int row) {
            if (objectCreator != null) {
                object = (T) objectCreator.get();
            }
        }

        @Override
        public void accept(int row, int column, char[] bytes, int off, int len) {
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
}
