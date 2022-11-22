package com.alibaba.fastjson2.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class CSVParser {
    final JSONReader jsonReader;
    Type[] types;
    ObjectReader[] typeReaders;
    ObjectReaderAdapter objectReader;

    List<String> columns;

    CSVParser(JSONReader jsonReader, ObjectReaderAdapter objectReader) {
        this.jsonReader = jsonReader;
        this.objectReader = objectReader;
    }

    public CSVParser(JSONReader jsonReader, Type[] types) {
        this.jsonReader = jsonReader;
        this.types = types;

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader[] readers = new ObjectReader[types.length];
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            if (type == String.class || type == Object.class) {
                readers[i] = null;
            } else {
                readers[i] = provider.getObjectReader(type);
            }
        }
        this.typeReaders = readers;
    }

    public static CSVParser of(String input, Class objectClass) {
        JSONReader.Context context = JSONFactory.createReadContext();
        JSONReader jsonReader = JSONReader.ofCSV(input, context);
        ObjectReaderAdapter objectReader = (ObjectReaderAdapter) context.getObjectReader(objectClass);
        return new CSVParser(jsonReader, objectReader);
    }

    public static CSVParser of(String input, Type... types) {
        JSONReader jsonReader = JSONReader.ofCSV(input);
        return new CSVParser(jsonReader, types);
    }

    public static CSVParser of(byte[] utf8Bytes, Type... types) {
        JSONReader jsonReader = JSONReader.ofCSV(utf8Bytes);
        return new CSVParser(jsonReader, types);
    }

    public List<String> readHeader() {
        columns = new ArrayList<>();

        for (int i = 0; ; ++i) {
            if (jsonReader.isEnd()) {
                break;
            }
            char ch = jsonReader.current();
            if (ch == '\r') {
                jsonReader.next();
                ch = jsonReader.current();
                if (ch != '\n') {
                    throw new JSONException("illegal format");
                }
                jsonReader.next();
                break;
            }

            if (ch == '\n') {
                jsonReader.next();
                break;
            }

            if (i != 0) {
                if (ch == ',') {
                    jsonReader.next();
                } else {
                    throw new JSONException("illegal format");
                }
            }
            String column = jsonReader.readString();
            columns.add(column);
        }

        if (objectReader != null) {
            Type[] types = new Type[columns.size()];
            ObjectReader[] typeReaders = new ObjectReader[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                FieldReader fieldReader = objectReader.getFieldReader(column);
                types[i] = fieldReader.fieldType;
                typeReaders[i] = fieldReader.getObjectReader(jsonReader);
            }
            this.types = types;
            this.typeReaders = typeReaders;
        }
        return columns;
    }

    public <T> T readLoneObject() {
        if (objectReader == null) {
            throw new JSONException("unsupported operation");
        }

        Object[] values = readLineValues();
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
            return (T) objectReader.createInstance(Arrays.asList(values));
        }
    }

    public Object[] readLineValues() {
        if (jsonReader.isEnd()) {
            return null;
        }

        Object[] values = null;
        List<Object> valueList = null;
        if (columns != null) {
            values = new Object[columns.size()];
        }

        for (int i = 0; ; ++i) {
            if (jsonReader.isEnd()) {
                break;
            }
            char ch = jsonReader.current();
            if (ch == '\r') {
                jsonReader.next();
                ch = jsonReader.current();
                if (ch != '\n') {
                    throw new JSONException("illegal format");
                }
                jsonReader.next();
                break;
            }

            if (ch == '\n') {
                jsonReader.next();
                break;
            }

            if (i != 0) {
                if (ch == ',') {
                    jsonReader.next();
                } else {
                    throw new JSONException("illegal format");
                }
            }

            Object value;
            ObjectReader reader = typeReaders != null && i < typeReaders.length
                    ? typeReaders[i]
                    : null;
            if (reader == null) {
                value = jsonReader.readString();
            } else {
                value = reader.readObject(jsonReader, null, null, 0L);
            }

            if (values == null) {
                if (valueList == null) {
                    valueList = new ArrayList<>();
                }
                valueList.add(value);
            } else {
                if (i < values.length) {
                    values[i] = value;
                }
            }
        }

        if (values == null) {
            if (valueList != null) {
                values = new Object[valueList.size()];
                valueList.toArray(values);
            }
        }
        return values;
    }

    public String[] readLine() {
        if (jsonReader.isEnd()) {
            return null;
        }

        String[] values = null;
        List<String> valueList = null;
        if (columns != null) {
            values = new String[columns.size()];
        }

        for (int i = 0; ; ++i) {
            if (jsonReader.isEnd()) {
                break;
            }
            char ch = jsonReader.current();
            if (ch == '\r') {
                jsonReader.next();
                ch = jsonReader.current();
                if (ch != '\n') {
                    throw new JSONException("illegal format");
                }
                jsonReader.next();
                break;
            }

            if (ch == '\n') {
                jsonReader.next();
                break;
            }

            if (i != 0) {
                if (ch == ',') {
                    jsonReader.next();
                } else {
                    throw new JSONException("illegal format");
                }
            }

            String value = jsonReader.readString();
            if (values == null) {
                if (valueList == null) {
                    valueList = new ArrayList<>();
                }
                valueList.add(value);
            } else {
                values[i] = value;
            }
        }

        if (values == null) {
            if (valueList != null) {
                values = new String[valueList.size()];
                valueList.toArray(values);
            }
        }
        return values;
    }

    public static int rowCount(String str) {
        State state = new State();
        state.rowCount(str, str.length());
        return state.rowCount();
    }

    public static int rowCount(byte[] bytes) {
        State state = new State();
        state.rowCount(bytes, bytes.length);
        return state.rowCount();
    }

    public static int rowCount(char[] chars) {
        State state = new State();
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
        final int SIZE_4M = 1024 * 256;
        byte[] bytes = new byte[SIZE_4M];

        State state = new State();
        while (true) {
            int cnt = in.read(bytes);
            if (cnt == -1) {
                break;
            }
            state.rowCount(bytes, cnt);
        }

        return state.rowCount();
    }

    static class State {
        boolean quote;
        int lineSize;
        int rowCount;

        public int rowCount() {
            return lineSize > 0 ? rowCount + 1 : rowCount;
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
                    if (lineSize > 0) {
                        rowCount++;
                        lineSize = 0;
                    }
                } else if (ch == '\r') {
                    if (lineSize > 0) {
                        rowCount++;
                        lineSize = 0;
                    }
                    int n = i + 1;
                    if (n >= length) {
                        break;
                    }
                    char next = bytes.charAt(n);
                    if (next == '\n') {
                        i++;
                    }
                    lineSize = 0;
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
                    if (lineSize > 0) {
                        rowCount++;
                        lineSize = 0;
                    }
                } else if (ch == '\r') {
                    if (lineSize > 0) {
                        rowCount++;
                        lineSize = 0;
                    }
                    int n = i + 1;
                    if (n >= length) {
                        break;
                    }
                    byte next = bytes[n];
                    if (next == '\n') {
                        i++;
                    }
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
                    if (lineSize > 0) {
                        rowCount++;
                        lineSize = 0;
                    }
                } else if (ch == '\r') {
                    if (lineSize > 0) {
                        rowCount++;
                        lineSize = 0;
                    }
                    int n = i + 1;
                    if (n >= length) {
                        break;
                    }
                    char next = bytes[n];
                    if (next == '\n') {
                        i++;
                    }
                } else {
                    lineSize++;
                }
            }
        }
    }
}
