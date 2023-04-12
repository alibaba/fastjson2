package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.stream.StreamReader;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import static com.alibaba.fastjson2.util.JDKUtils.*;

public abstract class CSVReader<T>
        extends StreamReader
        implements Closeable {
    boolean quote;
    protected Class<T> objectClass;

    CSVReader() {
    }

    CSVReader(Class<T> objectClass) {
        this.objectClass = objectClass;
    }

    public CSVReader(Type[] types) {
        super(types);
    }

    public void config(Feature... features) {
        for (Feature feature : features) {
            this.features |= feature.mask;
        }
    }

    public void config(Feature feature, boolean state) {
        if (state) {
            this.features |= feature.mask;
        } else {
            this.features &= ~feature.mask;
        }
    }

    public static <T> CSVReader<T> of(Reader reader, Class<T> objectClass) {
        return new CSVReaderUTF16(reader, objectClass);
    }

    public static <T> CSVReader of(String str, Class<T> objectClass) {
        if (JVM_VERSION > 8 && STRING_VALUE != null) {
            try {
                int coder = STRING_CODER.applyAsInt(str);
                if (coder == 0) {
                    byte[] bytes = STRING_VALUE.apply(str);
                    return new CSVReaderUTF8(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1, objectClass);
                }
            } catch (Exception e) {
                throw new JSONException("unsafe get String.coder error");
            }
        }

        char[] chars = JDKUtils.getCharArray(str);
        return new CSVReaderUTF16(chars, 0, chars.length, objectClass);
    }

    public static <T> CSVReader<T> of(char[] chars, Class<T> objectClass) {
        return new CSVReaderUTF16(chars, 0, chars.length, objectClass);
    }

    public static <T> CSVReader<T> of(byte[] utf8Bytes, Class<T> objectClass) {
        return of(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.UTF_8, objectClass);
    }

    public static CSVReader of(File file, Type... types) throws IOException {
        return new CSVReaderUTF8(new FileInputStream(file), StandardCharsets.UTF_8, types);
    }

    public static CSVReader of(File file, ByteArrayValueConsumer consumer) throws IOException {
        return of(file, StandardCharsets.UTF_8, consumer);
    }

    public static CSVReader of(File file, Charset charset, ByteArrayValueConsumer consumer) throws IOException {
        return new CSVReaderUTF8(new FileInputStream(file), charset, consumer);
    }

    public static CSVReader of(File file, CharArrayValueConsumer consumer) throws IOException {
        return of(file, StandardCharsets.UTF_8, consumer);
    }

    public static CSVReader of(File file, Charset charset, CharArrayValueConsumer consumer) throws IOException {
        return new CSVReaderUTF16(new InputStreamReader(new FileInputStream(file), charset), consumer);
    }

    public static CSVReader of(File file, Charset charset, Type... types) throws IOException {
        if (JDKUtils.JVM_VERSION == 8
                || charset == StandardCharsets.UTF_16
                || charset == StandardCharsets.UTF_16LE
                || charset == StandardCharsets.UTF_16BE
        ) {
            return new CSVReaderUTF16(
                    new InputStreamReader(new FileInputStream(file), charset), types
            );
        }

        return new CSVReaderUTF8(new FileInputStream(file), charset, types);
    }

    public static <T> CSVReader<T> of(File file, Class<T> objectClass) throws IOException {
        return of(file, StandardCharsets.UTF_8, objectClass);
    }

    public static <T> CSVReader<T> of(File file, Charset charset, Class<T> objectClass) throws IOException {
        if (JDKUtils.JVM_VERSION == 8
                || charset == StandardCharsets.UTF_16
                || charset == StandardCharsets.UTF_16LE
                || charset == StandardCharsets.UTF_16BE) {
            return new CSVReaderUTF16(
                    new InputStreamReader(
                            new FileInputStream(file),
                            charset
                    ),
                    objectClass
            );
        }

        return new CSVReaderUTF8(new FileInputStream(file), charset, objectClass);
    }

    public static CSVReader of(InputStream in, Type... types) throws IOException {
        return of(in, StandardCharsets.UTF_8, types);
    }

    public static <T> CSVReader<T> of(InputStream in, Class<T> objectClass) {
        return of(in, StandardCharsets.UTF_8, objectClass);
    }

    public static <T> CSVReader<T> of(InputStream in, Charset charset, Class<T> objectClass) {
        if (JDKUtils.JVM_VERSION == 8
                || charset == StandardCharsets.UTF_16
                || charset == StandardCharsets.UTF_16LE
                || charset == StandardCharsets.UTF_16BE
        ) {
            return new CSVReaderUTF16(
                    new InputStreamReader(in, charset),
                    objectClass
            );
        }

        return new CSVReaderUTF8(in, charset, objectClass);
    }

    public static CSVReader of(InputStream in, Charset charset, Type... types) throws IOException {
        if (JDKUtils.JVM_VERSION == 8
                || charset == StandardCharsets.UTF_16
                || charset == StandardCharsets.UTF_16LE
                || charset == StandardCharsets.UTF_16BE) {
            return new CSVReaderUTF16(
                    new InputStreamReader(in, charset), types
            );
        }

        return new CSVReaderUTF8(in, charset, types);
    }

    public static CSVReader of(Reader in, Type... types) throws IOException {
        return new CSVReaderUTF16(in, types);
    }

    public static CSVReader of(String str, Type... types) {
        if (JVM_VERSION > 8 && STRING_VALUE != null) {
            try {
                int coder = STRING_CODER.applyAsInt(str);
                if (coder == 0) {
                    byte[] bytes = STRING_VALUE.apply(str);
                    return new CSVReaderUTF8(bytes, 0, bytes.length, types);
                }
            } catch (Exception e) {
                throw new JSONException("unsafe get String.coder error");
            }
        }

        char[] chars = JDKUtils.getCharArray(str);
        return new CSVReaderUTF16(chars, 0, chars.length, types);
    }

    public static CSVReader of(char[] chars, Type... types) {
        return new CSVReaderUTF16(chars, 0, chars.length, types);
    }

    public static <T> CSVReader<T> of(
            char[] chars,
            int off,
            int len,
            CharArrayValueConsumer consumer
    ) {
        return new CSVReaderUTF16(chars, off, len, consumer);
    }

    public static CSVReader of(byte[] utf8Bytes, Type... types) {
        return new CSVReaderUTF8(utf8Bytes, 0, utf8Bytes.length, types);
    }

    public static CSVReader of(byte[] utf8Bytes, ByteArrayValueConsumer consumer) {
        return of(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.UTF_8, consumer);
    }

    public static <T> CSVReader<T> of(
            byte[] utf8Bytes,
            int off,
            int len,
            Charset charset, ByteArrayValueConsumer consumer
    ) {
        return new CSVReaderUTF8(utf8Bytes, off, len, charset, consumer);
    }

    public static <T> CSVReader<T> of(byte[] utf8Bytes, Charset charset, Class<T> objectClass) {
        return of(utf8Bytes, 0, utf8Bytes.length, charset, objectClass);
    }

    public static <T> CSVReader<T> of(byte[] utf8Bytes, int off, int len, Class<T> objectClass) {
        return new CSVReaderUTF8(utf8Bytes, off, len, StandardCharsets.UTF_8, objectClass);
    }

    public static <T> CSVReader<T> of(byte[] utf8Bytes, int off, int len, Charset charset, Class<T> objectClass) {
        if (charset == StandardCharsets.UTF_16
                || charset == StandardCharsets.UTF_16LE
                || charset == StandardCharsets.UTF_16BE
        ) {
            char[] chars = new char[len];
            int size = IOUtils.decodeUTF8(utf8Bytes, off, len, chars);
            return new CSVReaderUTF16(chars, 0, size, objectClass);
        }

        return new CSVReaderUTF8(utf8Bytes, off, len, charset, objectClass);
    }

    public static <T> CSVReader<T> of(char[] utf8Bytes, int off, int len, Class<T> objectClass) {
        return new CSVReaderUTF16(utf8Bytes, off, len, objectClass);
    }

    public void skipLines(int lines) throws IOException {
        if (lines < 0) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < lines; i++) {
            seekLine();
        }
    }

    public List<String> readHeader() {
        String[] columns = (String[]) readLineValues(true);

        if (objectClass != null) {
            ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
            boolean fieldBased = (features & JSONReader.Feature.FieldBased.mask) != 0;

            Type[] types = new Type[columns.length];
            ObjectReader[] typeReaders = new ObjectReader[columns.length];
            FieldReader[] fieldReaders = new FieldReader[columns.length];

            for (int i = 0; i < columns.length; i++) {
                String column = columns[i].trim();
                FieldReader fieldReader = provider.createFieldReader(objectClass, column, features);
                if (fieldReader != null) {
                    fieldReaders[i] = fieldReader;
                    Type fieldType = fieldReader.fieldType;
                    if (fieldType instanceof Class) {
                        Class fieldClass = (Class) fieldType;
                        if (fieldClass.isPrimitive()) {
                            fieldType = TypeUtils.nonePrimitive((Class) fieldType);
                        }
                    }
                    types[i] = fieldType;
                    typeReaders[i] = provider.getObjectReader(fieldType, fieldBased);
                } else {
                    types[i] = String.class;
                }
            }

            this.types = types;
            this.typeReaders = typeReaders;
            this.fieldReaders = fieldReaders;
            this.objectCreator = provider.createObjectCreator(objectClass, features);
        }

        this.columns = Arrays.asList(columns);
        this.columnStats = new ArrayList<>();
        for (int i = 0; i < columns.length; i++) {
            this.columnStats.add(new ColumnStat(columns[i]));
        }

        if (rowCount == 1) {
            rowCount = lineTerminated ? 0 : -1;
        }
        return this.columns;
    }

    public List<String> getColumns() {
        return columns;
    }

    public String getColumn(int columnIndex) {
        if (columns != null && columnIndex < columns.size()) {
            return columns.get(columnIndex);
        }
        return null;
    }

    public Type getColumnType(int columnIndex) {
        if (types != null && columnIndex < types.length) {
            return types[columnIndex];
        }
        return null;
    }

    public List<ColumnStat> getColumnStats() {
        return columnStats;
    }

    public void readLineObjectAll(Consumer<T> consumer) {
        readLineObjectAll(true, consumer);
    }

    public abstract void readLineObjectAll(boolean readHeader, Consumer<T> consumer);

    public T readLineObject() {
        if (inputEnd) {
            return null;
        }

        if (fieldReaders == null) {
            ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
            if (objectClass != null) {
                boolean fieldBased = (features & JSONReader.Feature.FieldBased.mask) != 0;
                ObjectReader objectReader = provider.getObjectReader(objectClass, fieldBased);
                if (objectReader instanceof ObjectReaderAdapter) {
                    this.fieldReaders = ((ObjectReaderAdapter) objectReader).getFieldReaders();
                    this.types = new Type[fieldReaders.length];
                    for (int i = 0; i < this.types.length; i++) {
                        types[i] = fieldReaders[i].fieldType;
                    }
                } else {
                    throw new JSONException("not support operation : " + objectClass);
                }
            } else {
                throw new JSONException("not support operation, objectClass is null");
            }
            objectCreator = provider.createObjectCreator(objectClass, features);
        }

        if (objectCreator == null) {
            throw new JSONException("not support operation, objectClass is null");
        }

        Object[] values = readLineValues(false);
        if (values == null) {
            return null;
        }

        if (fieldReaders != null) {
            Object object = objectCreator.get();
            for (int i = 0; i < this.fieldReaders.length; i++) {
                FieldReader fieldReader = fieldReaders[i];
                if (fieldReader != null) {
                    fieldReader.accept(object, values[i]);
                }
            }
            return (T) object;
        }

        throw new JSONException("not support operation, objectClass is null");
    }

    public abstract boolean isEnd();

    public final Object[] readLineValues() {
        return readLineValues(false);
    }

    protected abstract Object[] readLineValues(boolean strings);

    public final String[] readLine() {
        return (String[]) readLineValues(true);
    }

    public static int rowCount(String str, Feature... features) {
        CSVReader state = new CSVReaderUTF8(features);
        state.rowCount(str, str.length());
        return state.rowCount();
    }

    public static int rowCount(byte[] bytes, Feature... features) {
        CSVReaderUTF8 state = new CSVReaderUTF8(features);
        state.rowCount(bytes, bytes.length);
        return state.rowCount();
    }

    public static int rowCount(char[] chars, Feature... features) {
        CSVReaderUTF16 state = new CSVReaderUTF16(features);
        state.rowCount(chars, chars.length);
        return state.rowCount();
    }

    public static int rowCount(File file) throws IOException {
        if (!file.exists()) {
            return -1;
        }

        try (FileInputStream in = new FileInputStream(file)) {
            return rowCount(in);
        }
    }

    public static int rowCount(InputStream in) throws IOException {
        byte[] bytes = new byte[SIZE_512K];

        CSVReaderUTF8 state = new CSVReaderUTF8();
        while (true) {
            int cnt = in.read(bytes);
            if (cnt == -1) {
                break;
            }
            state.rowCount(bytes, cnt);
        }

        return state.rowCount();
    }

    public int errorCount() {
        return errorCount;
    }

    public int rowCount() {
        return lineTerminated ? rowCount : rowCount + 1;
    }

    void rowCount(String bytes, int length) {
        lineTerminated = false;
        for (int i = 0; i < length; i++) {
            char ch = bytes.charAt(i);
            if (ch == '"') {
                lineSize++;
                if (!quote) {
                    quote = true;
                } else {
                    int n = i + 1;
                    if (n >= length) {
                        break;
                    }
                    char next = bytes.charAt(n);
                    if (next == '"') {
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
                    lineSize = 0;
                }

                lineTerminated = i + 1 == length;
            } else if (ch == '\r') {
                lineTerminated = true;
                if (lineSize > 0 || (features & Feature.IgnoreEmptyLine.mask) == 0) {
                    rowCount++;
                }

                lineSize = 0;
                int n = i + 1;
                if (n >= length) {
                    break;
                }
                char next = bytes.charAt(n);
                if (next == '\n') {
                    i++;
                }

                lineTerminated = i + 1 == length;
            } else {
                lineSize++;
            }
        }
    }

    void rowCount(byte[] bytes, int length) {
        lineTerminated = false;

        for (int i = 0; i < length; i++) {
            if (i + 4 < length) {
                byte b0 = bytes[i];
                byte b1 = bytes[i + 1];
                byte b2 = bytes[i + 2];
                byte b3 = bytes[i + 3];
                if (b0 > '"'
                        && b1 > '"'
                        && b2 > '"'
                        && b3 > '"'
                ) {
                    lineSize += 4;
                    i += 3;
                    continue;
                }
            }

            byte ch = bytes[i];
            if (ch == '"') {
                lineSize++;
                if (!quote) {
                    quote = true;
                } else {
                    int n = i + 1;
                    if (n >= length) {
                        break;
                    }
                    byte next = bytes[n];
                    if (next == '"') {
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

                lineSize = 0;
                lineTerminated = i + 1 == length;
            } else if (ch == '\r') {
                if (lineSize > 0 || (features & Feature.IgnoreEmptyLine.mask) == 0) {
                    rowCount++;
                }
                lineTerminated = true;
                lineSize = 0;

                int n = i + 1;
                if (n >= length) {
                    break;
                }
                byte next = bytes[n];
                if (next == '\n') {
                    i++;
                }

                lineTerminated = i + 1 == length;
            } else {
                lineSize++;
            }
        }
    }

    void rowCount(char[] bytes, int length) {
        lineTerminated = false;
        for (int i = 0; i < length; i++) {
            if (i + 4 < length) {
                char b0 = bytes[i];
                char b1 = bytes[i + 1];
                char b2 = bytes[i + 2];
                char b3 = bytes[i + 3];
                if (b0 > '"'
                        && b1 > '"'
                        && b2 > '"'
                        && b3 > '"'
                ) {
                    i += 3;
                    lineSize += 4;
                    continue;
                }
            }

            char ch = bytes[i];
            if (ch == '"') {
                lineSize++;
                if (!quote) {
                    quote = true;
                } else {
                    int n = i + 1;
                    if (n >= length) {
                        break;
                    }
                    char next = bytes[n];
                    if (next == '"') {
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
                lineSize = 0;

                lineTerminated = i + 1 == length;
            } else if (ch == '\r' || (features & Feature.IgnoreEmptyLine.mask) == 0) {
                if (lineSize > 0) {
                    rowCount++;
                }
                lineTerminated = true;
                lineSize = 0;

                int n = i + 1;
                if (n >= length) {
                    break;
                }
                char next = bytes[n];
                if (next == '\n') {
                    i++;
                }

                lineTerminated = i + 1 == length;
            } else {
                lineSize++;
            }
        }
    }

    protected Object error(int columnIndex, Exception e) {
        errorCount++;
        getColumnStat(columnIndex).errors++;

        if ((features & Feature.ErrorAsNull.mask) != 0) {
            return null;
        }

        String message = "read csv error, line " + rowCount + ", column ";
        String column = null;
        if (columns != null && columnIndex < columns.size()) {
            column = columns.get(columnIndex);
        }
        if (column != null && !column.isEmpty()) {
            message += column;
        } else {
            message += columnIndex;
        }
        throw new JSONException(message, e);
    }

    public ColumnStat getColumnStat(String name) {
        if (columnStats != null) {
            for (ColumnStat stat : columnStats) {
                if (name.equals(stat.name)) {
                    return stat;
                }
            }
        }
        return null;
    }

    public ColumnStat getColumnStat(int i) {
        if (columnStats == null) {
            columnStats = new ArrayList<>();
        }

        StreamReader.ColumnStat stat = null;
        if (i >= columnStats.size()) {
            for (int j = columnStats.size(); j <= i; j++) {
                String column = null;
                if (columns != null && i < columns.size()) {
                    column = columns.get(i);
                }
                stat = new ColumnStat(column);
                columnStats.add(stat);
            }
        } else {
            stat = columnStats.get(i);
        }
        return stat;
    }

    public abstract void statAll();

    public abstract void readAll();
}
