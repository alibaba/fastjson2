package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.DateUtils;

import java.math.BigInteger;
import java.time.*;
import java.util.TimeZone;

import static com.alibaba.fastjson2.util.DateUtils.localDateTime;
import static java.time.ZoneOffset.UTC;

final class JSONReaderASCIICSV
        extends JSONReaderASCII {
    JSONReaderASCIICSV(Context ctx, String str, byte[] bytes, int offset, int length) {
        super(ctx, str, bytes, offset, length);
    }

    @Override
    public boolean isArray() {
        return offset - 1 == start || bytes[offset - 1] == '\n';
    }

    @Override
    public boolean isCSV() {
        return true;
    }

    public boolean isInt() {
        if (ch == '-' || ch == '+' || (ch >= '0' && ch <= '9')) {
            for (int i = offset; i < end; ++i) {
                byte ch = bytes[i];
                if (ch == ',' || ch == '\r' || ch == '\n') {
                    break;
                }
                if (ch < '0' || ch > '9') {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isNumber() {
        if (ch == '-' || ch == '+' || (ch >= '0' && ch <= '9')) {
            boolean dot = false;
            for (int i = offset; i < end; ++i) {
                byte ch = bytes[i];
                if (ch == ',' || ch == '\r' || ch == '\n') {
                    break;
                }
                if (ch == '.') {
                    if (dot) {
                        return false;
                    }
                    dot = true;
                } else if (ch != 'e'
                        && ch != 'E'
                        && ch != '+'
                        && ch != '-'
                        && (ch < '0' || ch > '9')) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isString() {
        return this.ch != ',' && this.ch != '\r' && this.ch != '\n';
    }

    @Override
    public boolean nextIfMatch(char ch) {
        if (this.ch != ch || offset >= end) {
            return false;
        }
        this.ch = (char) bytes[offset++];
        return true;
    }

    public int getStringLength() {
        boolean quote = this.ch == '"';
        int len = 1;
        if (!quote) {
            for (int i = offset; i < end; ++i, len++) {
                byte ch = bytes[i];
                if (ch == ',' || ch == '\r' || ch == '\n') {
                    break;
                }
            }
        } else {
            for (int i = offset; i < end; ++i, ++len) {
                byte ch = bytes[i];
                if (ch == '"') {
                    if (i + 1 < end && bytes[i + 1] == '"') {
                        ++i;
                        ++len;
                        continue;
                    }
                    break;
                }
            }
        }
        return len;
    }

    @Override
    public String readString() {
        if (ch == ',') {
            next();
            return null;
        }

        if (ch == '\n' || ch == '\r' || isEnd()) {
            return null;
        }

        int len = 0;
        String str;
        if (this.ch != '"') {
            for (int i = offset; i < end; ++i, len++) {
                char ch = (char) bytes[i];
                if (ch == ',' || ch == '\r' || ch == '\n') {
                    break;
                }
            }
            str = new String(bytes, offset - 1, len + 1);
            offset += len;
        } else {
            int escapeCount = 0;
            for (int i = offset; i < end; ++i, ++len) {
                char ch = (char) bytes[i];
                if (ch == '"') {
                    if (i + 1 < end && bytes[i + 1] == '"') {
                        ++i;
                        ++len;
                        escapeCount++;
                        continue;
                    }
                    break;
                }
            }

            if (escapeCount == 0) {
                str = new String(bytes, offset, len);
                offset += len + 1;
            } else {
                char[] chars = new char[len - escapeCount];
                for (int i = offset, end = offset + len, index = 0; i < end; ++i) {
                    char ch = (char) this.bytes[i];
                    if (ch == '"') {
                        if (i + 1 < end && this.bytes[i + 1] == '"') {
                            chars[index++] = ch;
                            ++i;
                            continue;
                        }
                        break;
                    }
                    chars[index++] = ch;
                }
                str = new String(chars);
                offset += len;
            }
        }

        ch = (char) bytes[offset++];
        return str;
    }

    @Override
    public int readInt32Value() {
        boolean negative = false;
        int firstOffset = offset;
        char firstChar = ch;

        int intValue = 0;

        if (ch == '-') {
            negative = true;
            ch = (char) this.bytes[offset++];
        } else if (ch == '+') {
            ch = (char) this.bytes[offset++];
        } else if (ch == ',') {
            return 0;
        }

        boolean overflow = false;
        while (ch >= '0' && ch <= '9') {
            int intValue10 = intValue * 10 + (ch - '0');
            if (intValue10 < intValue) {
                overflow = true;
                break;
            } else {
                intValue = intValue10;
            }
            if (offset == end) {
                ch = EOI;
                break;
            }
            ch = (char) this.bytes[offset++];
        }

        boolean notMatch = false;
        if (ch == '.'
                || ch == 'e'
                || ch == 'E'
                || ch == 't'
                || ch == 'f'
                || ch == 'n'
                || overflow) {
            notMatch = true;
        }

        if (notMatch) {
            this.offset = firstOffset;
            this.ch = firstChar;
            readNumber0();
            if (valueType == JSON_TYPE_INT) {
                BigInteger bigInteger = getBigInteger();
                try {
                    return bigInteger.intValueExact();
                } catch (ArithmeticException ex) {
                    throw new JSONException("int overflow, value " + bigInteger);
                }
            } else {
                if (valueType == JSON_TYPE_NULL && (context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                    throw new JSONException(info("int value not support input null"));
                }

                return getInt32Value();
            }
        }

        while (ch == ' ') {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) this.bytes[offset++];
            }
        }

        return negative ? -intValue : intValue;
    }

    @Override
    public long readInt64Value() {
        boolean negative = false;
        int firstOffset = offset;
        char firstChar = ch;

        long longValue = 0;

        if (ch == '-') {
            negative = true;
            ch = (char) this.bytes[offset++];
        } else if (ch == '+') {
            ch = (char) this.bytes[offset++];
        } else if (ch == ',') {
            return 0;
        }

        boolean overflow = false;
        while (ch >= '0' && ch <= '9') {
            long intValue10 = longValue * 10 + (ch - '0');
            if (intValue10 < longValue) {
                overflow = true;
                break;
            } else {
                longValue = intValue10;
            }
            if (offset >= end) {
                ch = EOI;
                break;
            }

            ch = (char) this.bytes[offset++];
        }

        boolean notMatch = false;
        if (ch == '.'
                || ch == 'e'
                || ch == 'E'
                || ch == 't'
                || ch == 'f'
                || ch == 'n'
                || overflow) {
            notMatch = true;
        }

        if (notMatch) {
            this.offset = firstOffset;
            this.ch = firstChar;
            readNumber0();
            if (valueType == JSON_TYPE_INT) {
                BigInteger bigInteger = getBigInteger();
                try {
                    return bigInteger.longValueExact();
                } catch (ArithmeticException ex) {
                    throw new JSONException("long overflow, value " + bigInteger);
                }
            } else {
                return getInt64Value();
            }
        }

        while (ch == ' ') {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) this.bytes[offset++];
            }
        }

        return negative ? -longValue : longValue;
    }

    @Override
    public void readNumber0() {
        this.wasNull = false;
        this.mag0 = 0;
        this.mag1 = 0;
        this.mag2 = 0;
        this.mag3 = 0;
        this.negative = false;
        this.exponent = 0;
        this.scale = 0;

        char quote = '\0';
        if (ch == '"') {
            quote = ch;
            ch = (char) this.bytes[offset++];

            if (ch == quote) {
                if (offset == end) {
                    ch = EOI;
                } else {
                    ch = (char) this.bytes[offset++];
                }
                nextIfMatch(',');
                wasNull = true;
                return;
            }
        } else if (ch == ',') {
            wasNull = true;
            valueType = JSON_TYPE_NULL;
            return;
        }

        final int start = offset;

        final int limit, multmin;
        if (ch == '-') {
            limit = Integer.MIN_VALUE;
            multmin = -214748364; // limit / 10;
            negative = true;
            ch = (char) this.bytes[offset++];
        } else {
            if (ch == '+') {
                ch = (char) this.bytes[offset++];
            }
            limit = -2147483647; // -Integer.MAX_VALUE;
            multmin = -214748364; // limit / 10;
        }

        // if (result < limit + digit) {
        boolean intOverflow = false;
        valueType = JSON_TYPE_INT;
        while (ch >= '0' && ch <= '9') {
            if (!intOverflow) {
                int digit = ch - '0';
                mag3 *= 10;
                if (mag3 < multmin
                        || mag3 < limit + digit) {
                    intOverflow = true;
                } else {
                    mag3 -= digit;
                    if (mag3 < multmin) {
                        intOverflow = true;
                    }
                }
            }
            if (offset == end) {
                ch = EOI;
                offset++;
                break;
            }
            ch = (char) this.bytes[offset++];
        }

        if (ch == '.') {
            valueType = JSON_TYPE_DEC;
            ch = (char) this.bytes[offset++];
            while (ch >= '0' && ch <= '9') {
                if (!intOverflow) {
                    int digit = ch - '0';
                    mag3 *= 10;
                    if (mag3 < multmin
                            || mag3 < limit + digit) {
                        intOverflow = true;
                    } else {
                        mag3 -= digit;
                        if (mag3 < multmin) {
                            intOverflow = true;
                        }
                    }
                }

                this.scale++;
                if (offset == end) {
                    ch = EOI;
                    offset++;
                    break;
                }
                ch = (char) this.bytes[offset++];
            }
        }

        if (intOverflow) {
            int numStart = negative ? start : start - 1;

            int numDigits = scale > 0 ? offset - 2 - numStart : offset - 1 - numStart;
            if (numDigits > 38) {
                valueType = JSON_TYPE_BIG_DEC;
                stringValue = new String(this.bytes, numStart, offset - 1 - numStart);
            } else {
                bigInt(this.bytes, numStart, offset - 1);
            }
        } else {
            mag3 = -mag3;
        }

        if (ch == 'e' || ch == 'E') {
            boolean negativeExp = false;
            int expValue = 0;
            ch = (char) this.bytes[offset++];

            if (ch == '-') {
                negativeExp = true;
                ch = (char) this.bytes[offset++];
            } else if (ch == '+') {
                ch = (char) this.bytes[offset++];
            }

            while (ch >= '0' && ch <= '9') {
                int byteVal = (ch - '0');
                expValue = expValue * 10 + byteVal;
                if (expValue > MAX_EXP) {
                    throw new JSONException("too large exp value : " + expValue);
                }

                if (offset == end) {
                    ch = EOI;
                    break;
                }
                ch = (char) this.bytes[offset++];
            }

            if (negativeExp) {
                expValue = -expValue;
            }

            this.exponent = (short) expValue;
            valueType = JSON_TYPE_DEC;
        }

        if (offset == start) {
            if (ch == 'n') {
                if (this.bytes[offset++] == 'u'
                        && this.bytes[offset++] == 'l'
                        && this.bytes[offset++] == 'l'
                ) {
                    wasNull = true;
                    valueType = JSON_TYPE_NULL;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = (char) this.bytes[offset++];
                    }
                }
            } else if (ch == 't') {
                if (this.bytes[offset++] == 'r'
                        && this.bytes[offset++] == 'u'
                        && this.bytes[offset++] == 'e'
                ) {
                    boolValue = true;
                    valueType = JSON_TYPE_BOOL;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = (char) this.bytes[offset++];
                    }
                }
            } else if (ch == 'f') {
                if (this.bytes[offset++] == 'a'
                        && this.bytes[offset++] == 'l'
                        && this.bytes[offset++] == 's'
                        && this.bytes[offset++] == 'e'
                ) {
                    boolValue = false;
                    valueType = JSON_TYPE_BOOL;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = (char) this.bytes[offset++];
                    }
                }
            } else if (ch == '{' && quote == 0) {
                this.complex = readObject();
                valueType = JSON_TYPE_OBJECT;
                return;
            } else if (ch == '[' && quote == 0) {
                this.complex = readArray();
                valueType = JSON_TYPE_ARRAY;
                return;
            }
        }

        if (quote != 0) {
            if (ch != quote) {
                this.offset -= 1;
                this.ch = quote;
                readString0();
                valueType = JSON_TYPE_STRING;
                return;
            }

            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) this.bytes[offset++];
            }
        }

        while (ch == ' ') {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) this.bytes[offset++];
            }
        }
    }

    public Integer readInt32() {
        if (ch == ',') {
            return null;
        }

        return readInt32Value();
    }

    public Long readInt64() {
        if (ch == ',') {
            return null;
        }

        return readInt64Value();
    }

    @Override
    public LocalDate readLocalDate10() {
        char c0 = ch;
        char c1 = (char) bytes[offset];
        char c2 = (char) bytes[offset + 1];
        char c3 = (char) bytes[offset + 2];
        char c4 = (char) bytes[offset + 3];
        char c5 = (char) bytes[offset + 4];
        char c6 = (char) bytes[offset + 5];
        char c7 = (char) bytes[offset + 6];
        char c8 = (char) bytes[offset + 7];
        char c9 = (char) bytes[offset + 8];

        char y0, y1, y2, y3, m0, m1, d0, d1;
        if (c4 == '-' && c7 == '-') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;
        } else if (c4 == '/' && c7 == '/') { // tw : yyyy/mm/dd
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;
        } else if (c2 == '.' && c5 == '.') {
            d0 = c0;
            d1 = c1;

            m0 = c3;
            m1 = c4;

            y0 = c6;
            y1 = c7;
            y2 = c8;
            y3 = c9;
        } else if (c2 == '-' && c5 == '-') {
            d0 = c0;
            d1 = c1;

            m0 = c3;
            m1 = c4;

            y0 = c6;
            y1 = c7;
            y2 = c8;
            y3 = c9;
        } else {
            return null;
        }

        int year;
        if (y0 >= '0' && y0 <= '9'
                && y1 >= '0' && y1 <= '9'
                && y2 >= '0' && y2 <= '9'
                && y3 >= '0' && y3 <= '9'
        ) {
            year = (y0 - '0') * 1000 + (y1 - '0') * 100 + (y2 - '0') * 10 + (y3 - '0');
        } else {
            return null;
        }

        int month;
        if (m0 >= '0' && m0 <= '9'
                && m1 >= '0' && m1 <= '9'
        ) {
            month = (m0 - '0') * 10 + (m1 - '0');
        } else {
            return null;
        }

        int dom;
        if (d0 >= '0' && d0 <= '9'
                && d1 >= '0' && d1 <= '9'
        ) {
            dom = (d0 - '0') * 10 + (d1 - '0');
        } else {
            return null;
        }

        if (year == 0 && month == 0 && dom == 0) {
            return null;
        }

        LocalDate ldt;
        try {
            ldt = LocalDate.of(year, month, dom);
        } catch (DateTimeException e) {
            throw new JSONException(info(), e);
        }

        offset += 9;
        this.ch = (char) this.bytes[offset++];
        return ldt;
    }

    @Override
    protected LocalDateTime readLocalDateTime19() {
        char c0 = ch;
        char c1 = (char) bytes[offset];
        char c2 = (char) bytes[offset + 1];
        char c3 = (char) bytes[offset + 2];
        char c4 = (char) bytes[offset + 3];
        char c5 = (char) bytes[offset + 4];
        char c6 = (char) bytes[offset + 5];
        char c7 = (char) bytes[offset + 6];
        char c8 = (char) bytes[offset + 7];
        char c9 = (char) bytes[offset + 8];
        char c10 = (char) bytes[offset + 9];
        char c11 = (char) bytes[offset + 10];
        char c12 = (char) bytes[offset + 11];
        char c13 = (char) bytes[offset + 12];
        char c14 = (char) bytes[offset + 13];
        char c15 = (char) bytes[offset + 14];
        char c16 = (char) bytes[offset + 15];
        char c17 = (char) bytes[offset + 16];
        char c18 = (char) bytes[offset + 17];

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2;
        if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = '0';
            S1 = '0';
            S2 = '0';
        } else if (c4 == '/' && c7 == '/' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = '0';
            S1 = '0';
            S2 = '0';
        } else {
            return null;
        }

        int year;
        if (y0 >= '0' && y0 <= '9'
                && y1 >= '0' && y1 <= '9'
                && y2 >= '0' && y2 <= '9'
                && y3 >= '0' && y3 <= '9'
        ) {
            year = (y0 - '0') * 1000 + (y1 - '0') * 100 + (y2 - '0') * 10 + (y3 - '0');
        } else {
            return null;
        }

        int month;
        if (m0 >= '0' && m0 <= '9'
                && m1 >= '0' && m1 <= '9'
        ) {
            month = (m0 - '0') * 10 + (m1 - '0');
        } else {
            return null;
        }

        int dom;
        if (d0 >= '0' && d0 <= '9'
                && d1 >= '0' && d1 <= '9'
        ) {
            dom = (d0 - '0') * 10 + (d1 - '0');
        } else {
            return null;
        }

        int hour;
        if (h0 >= '0' && h0 <= '9'
                && h1 >= '0' && h1 <= '9'
        ) {
            hour = (h0 - '0') * 10 + (h1 - '0');
        } else {
            return null;
        }

        int minute;
        if (i0 >= '0' && i0 <= '9'
                && i1 >= '0' && i1 <= '9'
        ) {
            minute = (i0 - '0') * 10 + (i1 - '0');
        } else {
            return null;
        }

        int second;
        if (s0 >= '0' && s0 <= '9'
                && s1 >= '0' && s1 <= '9'
        ) {
            second = (s0 - '0') * 10 + (s1 - '0');
        } else {
            return null;
        }

        int millis;
        if (S0 >= '0' && S0 <= '9'
                && S1 >= '0' && S1 <= '9'
                && S2 >= '0' && S2 <= '9'
        ) {
            millis = (S0 - '0') * 100
                    + (S1 - '0') * 10
                    + (S2 - '0');
            millis *= 1000_000;
        } else {
            return null;
        }

        if (year == 0 && month == 0 && dom == 0) {
            year = 1970;
            month = 1;
            dom = 1;
        }

        LocalDateTime ldt = LocalDateTime.of(year, month, dom, hour, minute, second, millis);

        offset += 18;
        this.ch = (char) this.bytes[offset++];
        return ldt;
    }

    @Override
    protected ZonedDateTime readZonedDateTimeX(int len) {
        if (len < 19) {
            return null;
        }

        char c0 = ch;
        char c1 = (char) bytes[offset];
        char c2 = (char) bytes[offset + 1];
        char c3 = (char) bytes[offset + 2];
        char c4 = (char) bytes[offset + 3];
        char c5 = (char) bytes[offset + 4];
        char c6 = (char) bytes[offset + 5];
        char c7 = (char) bytes[offset + 6];
        char c8 = (char) bytes[offset + 7];
        char c9 = (char) bytes[offset + 8];
        char c10 = (char) bytes[offset + 9];
        char c11 = (char) bytes[offset + 10];
        char c12 = (char) bytes[offset + 11];
        char c13 = (char) bytes[offset + 12];
        char c14 = (char) bytes[offset + 13];
        char c15 = (char) bytes[offset + 14];
        char c16 = (char) bytes[offset + 15];
        char c17 = (char) bytes[offset + 16];
        char c18 = (char) bytes[offset + 17];
        char c19 = len == 19 ? ' ' : (char) bytes[offset + 18];

        char c20, c21 = '0', c22 = '0', c23 = '0', c24 = '0', c25 = '0', c26 = '0', c27 = '0', c28 = '0', c29 = '\0';
        switch (len) {
            case 19:
                c20 = '\0';
                break;
            case 20:
                c20 = '\0';
                break;
            case 21:
                c20 = (char) bytes[offset + 19];
                break;
            case 22:
                c20 = (char) bytes[offset + 19];
                c21 = (char) bytes[offset + 20];
                break;
            case 23:
                c20 = (char) bytes[offset + 19];
                c21 = (char) bytes[offset + 20];
                c22 = (char) bytes[offset + 21];
                break;
            case 24:
                c20 = (char) bytes[offset + 19];
                c21 = (char) bytes[offset + 20];
                c22 = (char) bytes[offset + 21];
                c23 = (char) bytes[offset + 22];
                break;
            case 25:
                c20 = (char) bytes[offset + 19];
                c21 = (char) bytes[offset + 20];
                c22 = (char) bytes[offset + 21];
                c23 = (char) bytes[offset + 22];
                c24 = (char) bytes[offset + 23];
                break;
            case 26:
                c20 = (char) bytes[offset + 19];
                c21 = (char) bytes[offset + 20];
                c22 = (char) bytes[offset + 21];
                c23 = (char) bytes[offset + 22];
                c24 = (char) bytes[offset + 23];
                c25 = (char) bytes[offset + 24];
                break;
            case 27:
                c20 = (char) bytes[offset + 19];
                c21 = (char) bytes[offset + 20];
                c22 = (char) bytes[offset + 21];
                c23 = (char) bytes[offset + 22];
                c24 = (char) bytes[offset + 23];
                c25 = (char) bytes[offset + 24];
                c26 = (char) bytes[offset + 25];
                break;
            case 28:
                c20 = (char) bytes[offset + 19];
                c21 = (char) bytes[offset + 20];
                c22 = (char) bytes[offset + 21];
                c23 = (char) bytes[offset + 22];
                c24 = (char) bytes[offset + 23];
                c25 = (char) bytes[offset + 24];
                c26 = (char) bytes[offset + 25];
                c27 = (char) bytes[offset + 26];
                break;
            case 29:
                c20 = (char) bytes[offset + 19];
                c21 = (char) bytes[offset + 20];
                c22 = (char) bytes[offset + 21];
                c23 = (char) bytes[offset + 22];
                c24 = (char) bytes[offset + 23];
                c25 = (char) bytes[offset + 24];
                c26 = (char) bytes[offset + 25];
                c27 = (char) bytes[offset + 26];
                c28 = (char) bytes[offset + 27];
                break;
            default:
                c20 = (char) bytes[offset + 19];
                c21 = (char) bytes[offset + 20];
                c22 = (char) bytes[offset + 21];
                c23 = (char) bytes[offset + 22];
                c24 = (char) bytes[offset + 23];
                c25 = (char) bytes[offset + 24];
                c26 = (char) bytes[offset + 25];
                c27 = (char) bytes[offset + 26];
                c28 = (char) bytes[offset + 27];
                c29 = (char) bytes[offset + 28];
                break;
        }

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2, S3, S4, S5, S6, S7, S8;
        int zoneIdBegin;
        boolean isTimeZone = false;
        if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':'
                && (c19 == '[' || c19 == 'Z' || c19 == '+' || c19 == '-' || c19 == ' ')
        ) {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = '0';
            S1 = '0';
            S2 = '0';
            S3 = '0';
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 19;
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 21 || c21 == '[' || c21 == '+' || c21 == '-' || c21 == 'Z')) {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = c20;
            S1 = '0';
            S2 = '0';
            S3 = '0';
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 21;
            isTimeZone = c21 == '|';
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 22 || c22 == '[' || c22 == '+' || c22 == '-' || c22 == 'Z')) {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = c20;
            S1 = c21;
            S2 = '0';
            S3 = '0';
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 22;
            isTimeZone = c22 == '|';
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == 'Z' && c17 == '['
                && len == 22 && c21 == ']') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = '0';
            s1 = '0';

            S0 = '0';
            S1 = '0';
            S2 = '0';
            S3 = '0';
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            isTimeZone = true;
            zoneIdBegin = 17;
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 23 || c23 == '[' || c23 == '|' || c23 == '+' || c23 == '-' || c23 == 'Z')) {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = '0';
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 23;
            isTimeZone = c23 == '|';
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 24 || c24 == '[' || c24 == '|' || c24 == '+' || c24 == '-' || c24 == 'Z')) {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = c23;
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 24;
            isTimeZone = c24 == '|';
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 25 || c25 == '[' || c25 == '|' || c25 == '+' || c25 == '-' || c25 == 'Z')) {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = c23;
            S4 = c24;
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 25;
            isTimeZone = c25 == '|';
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 26 || c26 == '[' || c26 == '|' || c26 == '+' || c26 == '-' || c26 == 'Z')) {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = c23;
            S4 = c24;
            S5 = c25;
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 26;
            isTimeZone = c26 == '|';
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 27 || c27 == '[' || c27 == '|' || c27 == '+' || c27 == '-' || c27 == 'Z')) {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = c23;
            S4 = c24;
            S5 = c25;
            S6 = c26;
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 27;
            isTimeZone = c27 == '|';
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 28 || c28 == '[' || c28 == '|' || c28 == '+' || c28 == '-' || c28 == 'Z')) {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = c23;
            S4 = c24;
            S5 = c25;
            S6 = c26;
            S7 = c27;
            S8 = '0';
            zoneIdBegin = 28;
            isTimeZone = c28 == '|';
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 29 || c29 == '[' || c29 == '|' || c29 == '+' || c29 == '-' || c29 == 'Z')) {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = c23;
            S4 = c24;
            S5 = c25;
            S6 = c26;
            S7 = c27;
            S8 = c28;
            zoneIdBegin = 29;
            isTimeZone = c29 == '|';
        } else {
            return null;
        }

        char first = (char) bytes[this.offset + zoneIdBegin - 1];

        LocalDateTime ldt = localDateTime(y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2, S3, S4, S5, S6, S7, S8);

        ZoneId zoneId;
        if (isTimeZone) {
            String tzStr = new String(bytes, this.offset + zoneIdBegin, len - zoneIdBegin - 1);
            TimeZone timeZone = TimeZone.getTimeZone(tzStr);
            zoneId = timeZone.toZoneId();
        } else {
            if (first == 'Z') {
                zoneId = UTC;
            } else {
                String zoneIdStr;
                if (first == '+' || first == '-') {
                    zoneIdStr = new String(bytes, this.offset + zoneIdBegin - 1, len - zoneIdBegin);
                } else if (first == ' ') {
                    zoneIdStr = new String(bytes, this.offset + zoneIdBegin, len - zoneIdBegin - 1);
                } else { // '[
                    if (zoneIdBegin < len) {
                        zoneIdStr = new String(bytes, this.offset + zoneIdBegin, len - zoneIdBegin - 2);
                    } else {
                        zoneIdStr = null;
                    }
                }
                zoneId = DateUtils.getZoneId(zoneIdStr, context.zoneId);
            }
        }

        ZonedDateTime zdt = ZonedDateTime.ofLocal(ldt, zoneId, null);

        offset += len - 1;
        this.ch = (char) bytes[offset++];
        return zdt;
    }

    public void next() {
        this.ch = (char) this.bytes[offset++];
    }
}
