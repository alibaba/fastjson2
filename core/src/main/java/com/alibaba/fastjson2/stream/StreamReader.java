package com.alibaba.fastjson2.stream;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;

public abstract class StreamReader {
    protected static final int SIZE_256K = 1024 * 256;

    protected ObjectReaderProvider provider;
    protected long features;

    protected Type[] types;
    protected ObjectReader[] typeReaders;
    protected ObjectReaderAdapter objectReader;

    protected int lineSize;
    protected int rowCount;

    protected int lineStart;
    protected int lineEnd;
    protected int lineNextStart;

    protected int end;
    protected int off;

    protected boolean inputEnd;
    protected boolean lineTerminated;

    public StreamReader() {
    }

    public StreamReader(Type[] types) {
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

    public StreamReader(ObjectReaderAdapter objectReader) {
        this.objectReader = objectReader;
    }

    protected abstract boolean seekLine() throws IOException;

    public abstract <T> T readLineObject();

    public enum Feature {
        IgnoreEmptyLine(1),
        ErrorAsNull(1 << 1);

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }
    }

    public static class ColumnStat {
        @JSONField(ordinal = -1)
        public final String name;

        public int values;
        public int nulls;
        public int integers;
        public int numbers;
        public int dates;
        public int booleans;

        public int precision;
        public int scale;
        public int nonAsciiStrings;
        public int errors;

        public ColumnStat(String name) {
            this.name = name;
        }

        public void stat(String value) {
            values++;

            if (value == null || value.isEmpty()) {
                nulls++;
                return;
            }

            final int len = value.length();
            boolean nonAscii = false;
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                if (ch > 127) {
                    nonAscii = true;
                    break;
                }
            }
            if (nonAscii) {
                if (precision < len) {
                    precision = len;
                }
                nonAsciiStrings++;
                return;
            }

            int precision = len;
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                booleans++;
            } else if (TypeUtils.isNumber(value)) {
                char ch = value.charAt(0);
                if (ch == '+' || ch == '-') {
                    precision--;
                }
                numbers++;
                if (TypeUtils.isInteger(value)) {
                    integers++;
                } else {
                    int dotIndex = value.lastIndexOf('.');
                    if (dotIndex != -1) {
                        int scale = value.length() - dotIndex - 1;
                        if (this.scale < scale) {
                            this.scale = scale;
                        }
                        precision--;
                    }
                }
            } else {
                boolean checkDate = false;
                int sub = 0, slash = 0, colon = 0, dot = 0, nums = 0;
                for (int i = 0; i < value.length(); i++) {
                    char ch = value.charAt(i);
                    switch (ch) {
                        case '-':
                            sub++;
                            break;
                        case '/':
                            slash++;
                            break;
                        case ':':
                            colon++;
                            break;
                        case '.':
                            dot++;
                            break;
                        default:
                            if (ch >= '0' && ch <= '9') {
                                nums++;
                            }
                            break;
                    }
                }

                if (!checkDate && (sub == 2 || slash == 2 || colon == 2)) {
                    checkDate = true;
                }

                if (checkDate && (nums < 4 || len > 36)) {
                    checkDate = false;
                }

                if (checkDate) {
                    try {
                        ZonedDateTime zdt = DateUtils.parseZonedDateTime(value);
                        if (zdt != null) {
                            precision = 0;
                            dates++;
                        }
                        int nanoOfSeconds = zdt.getNano();
                        if (nanoOfSeconds != 0) {
                            if (nanoOfSeconds % 100000000 == 0) {
                                precision = 1;
                            } else if (nanoOfSeconds % 10000000 == 0) {
                                precision = 2;
                            } else if (nanoOfSeconds % 1000000 == 0) {
                                precision = 3;
                            } else if (nanoOfSeconds % 100000 == 0) {
                                precision = 4;
                            } else if (nanoOfSeconds % 10000 == 0) {
                                precision = 5;
                            } else if (nanoOfSeconds % 1000 == 0) {
                                precision = 6;
                            } else if (nanoOfSeconds % 100 == 0) {
                                precision = 7;
                            } else if (nanoOfSeconds % 10 == 0) {
                                precision = 8;
                            } else {
                                precision = 9;
                            }
                        }
                    } catch (Exception ignored) {
                        // ignored
                    }
                }
            }

            if (this.precision < precision) {
                this.precision = precision;
            }
        }

        public Type getInferType() {
            if (nonAsciiStrings > 0 || nulls == values) {
                return String.class;
            }

            if (values == booleans + nulls) {
                return Boolean.class;
            }

            if (values == dates + nulls) {
                if (precision != 0) {
                    return Instant.class;
                }
                return Date.class;
            }

            if (values == integers + nulls) {
                if (precision < 10) {
                    return Integer.class;
                }
                if (precision < 20) {
                    return Long.class;
                }
                return BigInteger.class;
            }

            if (values == numbers + nulls) {
                return BigDecimal.class;
            }

            return String.class;
        }
    }
}
