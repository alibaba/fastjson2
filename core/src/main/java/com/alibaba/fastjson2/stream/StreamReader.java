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
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    protected boolean lineTerminated = true;

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
        public int doubles;
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

        public void stat(byte[] bytes, int off, int len, Charset charset) {
            values++;

            if (len == 0) {
                nulls++;
                return;
            }

            int end = off + len;
            boolean nonAscii = false;
            for (int i = off; i < end; i++) {
                byte b = bytes[i];
                if (b < 0) {
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
            if (TypeUtils.isNumber(bytes, off, len)) {
                char ch = (char) bytes[off];
                if (ch == '+' || ch == '-') {
                    precision--;
                }
                numbers++;
                if (TypeUtils.isInteger(bytes, off, len)) {
                    integers++;
                } else {
                    boolean e = false;
                    int dotIndex = -1;
                    for (int i = off; i < end; i++) {
                        byte b = bytes[i];
                        if (b == '.') {
                            dotIndex = i;
                        } else if (b == 'e' || b == 'E') {
                            e = true;
                        }
                    }
                    if (e) {
                        doubles++;
                    } else if (dotIndex != -1) {
                        int scale = end - dotIndex - 1;
                        if (this.scale < scale) {
                            this.scale = scale;
                        }
                        precision--;
                    }
                }
            } else {
                boolean checkDate = false;
                int sub = 0, slash = 0, colon = 0, dot = 0, nums = 0;
                for (int i = off; i < end; i++) {
                    char ch = (char) bytes[i];
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

                if ((!checkDate) && (sub == 2 || slash == 2 || colon == 2)) {
                    checkDate = true;
                }

                if (checkDate && (nums < 4 || len > 36)) {
                    checkDate = false;
                }

                if (checkDate) {
                    try {
                        LocalDateTime ldt = null;
                        switch (len) {
                            case 8: {
                                LocalDate localDate = DateUtils.parseLocalDate8(bytes, off);
                                ldt = localDate.atStartOfDay();
                                break;
                            }
                            case 9: {
                                LocalDate localDate = DateUtils.parseLocalDate9(bytes, off);
                                ldt = localDate.atStartOfDay();
                                break;
                            }
                            case 10: {
                                LocalDate localDate = DateUtils.parseLocalDate10(bytes, off);
                                ldt = localDate.atStartOfDay();
                                break;
                            }
                            default:
                                break;
                        }

                        if (ldt == null) {
                            String str = new String(bytes, off, len, charset);
                            ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);
                            if (zdt != null) {
                                ldt = zdt.toLocalDateTime();
                            }
                        }
                        if (ldt != null) {
                            precision = 0;
                            dates++;
                        }
                        int nanoOfSeconds = ldt.getNano();
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
            if (TypeUtils.isNumber(value)) {
                char ch = value.charAt(0);
                if (ch == '+' || ch == '-') {
                    precision--;
                }
                numbers++;
                if (TypeUtils.isInteger(value)) {
                    integers++;
                } else {
                    boolean e = false;
                    int dotIndex = -1;
                    for (int i = 0; i < value.length(); i++) {
                        char b = value.charAt(i);
                        if (b == '.') {
                            dotIndex = i;
                        } else if (b == 'e' || b == 'E') {
                            e = true;
                        }
                    }
                    if (e) {
                        doubles++;
                    } else if (dotIndex != -1) {
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

                if ((!checkDate) && (sub == 2 || slash == 2 || colon == 2)) {
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

            if (doubles > 0) {
                return Double.class;
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
