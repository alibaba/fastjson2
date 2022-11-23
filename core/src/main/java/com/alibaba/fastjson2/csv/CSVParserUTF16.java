package com.alibaba.fastjson2.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class CSVParserUTF16
        extends CSVParser {
    char[] buf;
    Reader input;

    CSVParserUTF16(Feature... features) {
        for (Feature feature : features) {
            this.features |= feature.mask;
        }
    }

    CSVParserUTF16(
            char[] bytes,
            int off,
            int len,
            ObjectReaderAdapter objectReader
    ) {
        super(objectReader);
        this.buf = bytes;
        this.off = off;
        this.end = off + len;
    }

    CSVParserUTF16(
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

    void seekLine() throws IOException {
        if (buf == null) {
            if (input != null) {
                buf = new char[SIZE_256K];
                int cnt = input.read(buf);
                if (cnt == -1) {
                    inputEnd = true;
                    throw new IOException();
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
                    if (lineSize > 0) {
                        rowCount++;
                    }
                    lineSize = 0;
                    lineEnd = i;
                    lineStart = lineNextStart;
                    lineNextStart = off = i + 1;

                    lineTerminated = true;
                    break;
                } else if (ch == '\r') {
                    if (lineSize > 0) {
                        rowCount++;
                    }
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

                    lineTerminated = true;
                    break;
                } else {
                    lineSize++;
                }
            }

            if (!lineTerminated) {
                if (input != null && !inputEnd) {
                    int mark_off = off;
                    int mark_lineStart = lineStart;
                    int mark_lineNextStart = lineNextStart;
                    int mark_end = end;
                    boolean mark_quote = quote;

                    int len = end - off;
                    if (off > 0) {
                        System.arraycopy(buf, off, buf, 0, len);
                        lineStart = lineNextStart = 0;
                        off = 0;
                        end = len;
                        quote = false;
                    }

                    int cnt = input.read(buf, end, buf.length - end);
                    if (cnt == -1) {
                        inputEnd = true;
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

            break;
        }
    }

    public List<String> readHeader() {
        columns = new ArrayList<>();

        try {
            seekLine();
        } catch (IOException e) {
            throw new JSONException("seekLine error", e);
        }

        boolean quote = false;
        int valueStart = lineStart;
        int valueSize = 0;
        int escapeCount = 0;
        for (int i = lineStart; i < lineEnd; ++i) {
            char ch = buf[i];

            if (quote) {
                if (ch == '"') {
                    int n = i + 1;
                    if (n < lineEnd && buf[n] == '"') {
                        valueSize += 2;
                        escapeCount++;
                        ++i;
                        continue;
                    }
                }
            }

            if (ch == '"') {
                quote = true;
                continue;
            }

            if (ch == ',') {
                String column;
                if (quote) {
                    if (escapeCount == 0) {
                        column = new String(buf, valueStart + 1, valueSize);
                    } else {
                        throw new JSONException("TODO");
                    }
                } else {
                    column = new String(buf, valueStart, valueSize);
                }

                columns.add(column);
                quote = false;
                valueStart = i + 1;
                valueSize = 0;
                escapeCount = 0;
                continue;
            }

            valueSize++;
        }

        if (valueSize > 0) {
            String column;
            if (quote) {
                if (escapeCount == 0) {
                    column = new String(buf, valueStart + 1, valueSize);
                } else {
                    throw new JSONException("TODO");
                }
            } else {
                column = new String(buf, valueStart, valueSize);
            }
            columns.add(column);
        }

        if (objectReader != null) {
            JSONReader.Context context = JSONFactory.createReadContext(provider);
            Type[] types = new Type[columns.size()];
            ObjectReader[] typeReaders = new ObjectReader[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                FieldReader fieldReader = objectReader.getFieldReader(column);
                types[i] = fieldReader.fieldType;
                typeReaders[i] = fieldReader.getObjectReader(context);
            }
            this.types = types;
            this.typeReaders = typeReaders;
        }
        return columns;
    }

    Object readValue(char[] buf, int valueStart, int valueSize, Type type) {
        String str = new String(buf, valueStart, valueSize);
        return TypeUtils.cast(str, type);
    }

    public Object[] readLineValues(boolean strings) {
        try {
            if (off >= end) {
                return null;
            }
            seekLine();
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
                        if (type == null || type == String.class || type == Object.class) {
                            value = new String(buf, valueStart + 1, valueSize);
                        } else {
                            value = readValue(buf, valueStart + 1, valueSize, type);
                        }
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

                        if (type == null || type == String.class || type == Object.class) {
                            value = new String(bytes);
                        } else {
                            value = readValue(bytes, 0, bytes.length, type);
                        }
                    }
                } else {
                    if (type == null || type == String.class || type == Object.class) {
                        value = new String(buf, valueStart, valueSize);
                    } else {
                        value = readValue(buf, valueStart, valueSize, type);
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
                        value = new String(buf, valueStart + 1, valueSize);
                    } else {
                        value = readValue(buf, valueStart + 1, valueSize, type);
                    }
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

                    if (type == null || type == String.class || type == Object.class) {
                        value = new String(bytes);
                    } else {
                        value = readValue(bytes, 0, bytes.length, type);
                    }
                }
            } else {
                if (type == null || type == String.class || type == Object.class) {
                    value = new String(buf, valueStart, valueSize);
                } else {
                    value = readValue(buf, valueStart, valueSize, type);
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
}
