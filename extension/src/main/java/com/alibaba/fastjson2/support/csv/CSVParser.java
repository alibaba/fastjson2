package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;
import com.alibaba.fastjson2.stream.StreamReader;
import com.alibaba.fastjson2.util.JDKUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static com.alibaba.fastjson2.util.JDKUtils.*;

public abstract class CSVParser
        extends StreamReader
        implements Closeable {
    List<String> columns;
    List<ColumnStat> columnStats;
    Map<String, Type> schema;
    int rowCount;
    int errorCount;

    boolean quote;

    CSVParser() {
    }

    CSVParser(Map<String, Type> schema) {
        this.schema = schema;
    }

    CSVParser(ObjectReaderAdapter objectReader) {
        super(objectReader);
    }

    public CSVParser(Type[] types) {
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

    public static CSVParser of(Reader reader, Class objectClass) {
        JSONReader.Context context = JSONFactory.createReadContext();
        ObjectReaderAdapter objectReader = (ObjectReaderAdapter) context.getObjectReader(objectClass);
        return new CSVParserUTF16(reader, objectReader);
    }

    public static CSVParser of(String str, Class objectClass) {
        JSONReader.Context context = JSONFactory.createReadContext();
        ObjectReaderAdapter objectReader = (ObjectReaderAdapter) context.getObjectReader(objectClass);

        if (JVM_VERSION > 8 && STRING_VALUE != null) {
            try {
                int coder = STRING_CODER.applyAsInt(str);
                if (coder == 0) {
                    byte[] bytes = STRING_VALUE.apply(str);
                    return new CSVParserUTF8(bytes, 0, bytes.length, objectReader);
                }
            } catch (Exception e) {
                throw new JSONException("unsafe get String.coder error");
            }
        }

        char[] chars = JDKUtils.getCharArray(str);
        return new CSVParserUTF16(chars, 0, chars.length, objectReader);
    }

    public static CSVParser of(char[] chars, Class objectClass) {
        JSONReader.Context context = JSONFactory.createReadContext();
        ObjectReaderAdapter objectReader = (ObjectReaderAdapter) context.getObjectReader(objectClass);
        return new CSVParserUTF16(chars, 0, chars.length, objectReader);
    }

    public static CSVParser of(byte[] utf8Bytes, Class objectClass) {
        JSONReader.Context context = JSONFactory.createReadContext();
        ObjectReaderAdapter objectReader = (ObjectReaderAdapter) context.getObjectReader(objectClass);
        return new CSVParserUTF8(utf8Bytes, 0, utf8Bytes.length, objectReader);
    }

    public static CSVParser of(File file, Type... types) throws IOException {
        return new CSVParserUTF8(new FileInputStream(file), StandardCharsets.UTF_8, types);
    }

    public static CSVParser of(File file, Charset charset, Type... types) throws IOException {
        if (charset == StandardCharsets.UTF_16
                || charset == StandardCharsets.UTF_16LE
                || charset == StandardCharsets.UTF_16BE) {
            return new CSVParserUTF16(
                    new InputStreamReader(new FileInputStream(file), charset), types
            );
        }

        return new CSVParserUTF8(new FileInputStream(file), charset, types);
    }

    public static CSVParser of(File file, Class objectClass) throws IOException {
        return of(file, StandardCharsets.UTF_8, objectClass);
    }

    public static CSVParser of(File file, Charset charset, Class objectClass) throws IOException {
        JSONReader.Context context = JSONFactory.createReadContext();
        ObjectReaderAdapter objectReader = (ObjectReaderAdapter) context.getObjectReader(objectClass);

        if (charset == StandardCharsets.UTF_16
                || charset == StandardCharsets.UTF_16LE
                || charset == StandardCharsets.UTF_16BE) {
            return new CSVParserUTF16(
                    new InputStreamReader(new FileInputStream(file), charset), objectReader
            );
        }

        return new CSVParserUTF8(new FileInputStream(file), charset, objectReader);
    }

    public static CSVParser of(InputStream in, Type... types) throws IOException {
        return new CSVParserUTF8(in, StandardCharsets.UTF_8, types);
    }

    public static CSVParser of(InputStream in, Class objectClass) {
        return of(in, StandardCharsets.UTF_8, objectClass);
    }

    public static CSVParser of(InputStream in, Charset charset, Class objectClass) {
        JSONReader.Context context = JSONFactory.createReadContext();
        ObjectReaderAdapter objectReader = (ObjectReaderAdapter) context.getObjectReader(objectClass);
        return new CSVParserUTF8(in, charset, objectReader);
    }

    public static CSVParser of(InputStream in, Charset charset, Type... types) throws IOException {
        if (charset == StandardCharsets.UTF_16
                || charset == StandardCharsets.UTF_16LE
                || charset == StandardCharsets.UTF_16BE) {
            return new CSVParserUTF16(
                    new InputStreamReader(in, charset), types
            );
        }

        return new CSVParserUTF8(in, charset, types);
    }

    public static CSVParser of(Reader in, Type... types) throws IOException {
        return new CSVParserUTF16(in, types);
    }

    public static CSVParser of(File file, Map<String, Type> types) throws IOException {
        return new CSVParserUTF8(new FileInputStream(file), types);
    }

    public static CSVParser of(String str, Type... types) {
        if (JVM_VERSION > 8 && STRING_VALUE != null) {
            try {
                int coder = STRING_CODER.applyAsInt(str);
                if (coder == 0) {
                    byte[] bytes = STRING_VALUE.apply(str);
                    return new CSVParserUTF8(bytes, 0, bytes.length, types);
                }
            } catch (Exception e) {
                throw new JSONException("unsafe get String.coder error");
            }
        }

        char[] chars = JDKUtils.getCharArray(str);
        return new CSVParserUTF16(chars, 0, chars.length, types);
    }

    public static CSVParser of(char[] chars, Type... types) {
        return new CSVParserUTF16(chars, 0, chars.length, types);
    }

    public static CSVParser of(byte[] utf8Bytes, Type... types) {
        return new CSVParserUTF8(utf8Bytes, 0, utf8Bytes.length, types);
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

        if (objectReader != null) {
            JSONReader.Context context = JSONFactory.createReadContext(provider);
            Type[] types = new Type[columns.length];
            ObjectReader[] typeReaders = new ObjectReader[columns.length];
            for (int i = 0; i < columns.length; i++) {
                String column = columns[i];
                FieldReader fieldReader = objectReader.getFieldReader(column);
                types[i] = fieldReader.fieldType;
                typeReaders[i] = fieldReader.getObjectReader(context);
            }
            this.types = types;
            this.typeReaders = typeReaders;
        }

        this.columns = Arrays.asList(columns);
        this.columnStats = new ArrayList<>();
        for (int i = 0; i < columns.length; i++) {
            this.columnStats.add(new ColumnStat(columns[i]));
        }

        if (rowCount == 1) {
            rowCount = 0;
        }
        return this.columns;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<ColumnStat> getColumnStats() {
        return columnStats;
    }

    public <T> Stream<T> readAsStream() {
        return readAsStream(Integer.MAX_VALUE);
    }

    public <T> Stream<T> readAsStream(int limit) {
        return StreamUtil.stream(c -> {
            T t;
            for (int left = limit; left > 0 && (t = readLineObject()) != null; left--) {
                c.accept(t);
            }
        });
    }

    public <T> T readLineObject() {
        if (inputEnd) {
            return null;
        }

        if (objectReader == null) {
            throw new JSONException("unsupported operation");
        }

        if (types == null) {
            FieldReader[] fieldReaders = objectReader.getFieldReaders();
            Type[] types = new Type[fieldReaders.length];
            for (int i = 0; i < fieldReaders.length; i++) {
                types[i] = fieldReaders[i].fieldType;
            }
            this.types = types;
        }

        Object[] values = readLineValues(false);
        if (values == null) {
            return null;
        }

        if (columns != null) {
            Map map = new HashMap();
            for (int i = 0; i < values.length; i++) {
                if (i < columns.size()) {
                    String column = columns.get(i);
                    map.put(column, values[i]);
                }
            }
            return (T) objectReader.createInstance(map);
        } else {
            return (T) objectReader.createInstance(values == null ? Collections.emptyList() : Arrays.asList(values));
        }
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
        CSVParser state = new CSVParserUTF8(features);
        state.rowCount(str, str.length());
        return state.rowCount();
    }

    public static int rowCount(byte[] bytes, Feature... features) {
        CSVParserUTF8 state = new CSVParserUTF8(features);
        state.rowCount(bytes, bytes.length);
        return state.rowCount();
    }

    public static int rowCount(char[] chars, Feature... features) {
        CSVParserUTF16 state = new CSVParserUTF16(features);
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
        byte[] bytes = new byte[SIZE_256K];

        CSVParserUTF8 state = new CSVParserUTF8();
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

    protected ColumnStat getColumnStat(int i) {
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

    public void statAll() {
        String[] line;
        while ((line = readLine()) != null) {
            for (int i = 0; i < line.length; i++) {
                String value = line[i];
                if (columnStats == null) {
                    columnStats = new ArrayList<>();
                }
                StreamReader.ColumnStat stat = getColumnStat(i);
                stat.stat(value);
            }
        }
    }
}
