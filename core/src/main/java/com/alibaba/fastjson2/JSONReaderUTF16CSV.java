package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.DateUtils;

import java.math.BigInteger;
import java.time.*;
import java.util.TimeZone;

import static com.alibaba.fastjson2.util.DateUtils.localDateTime;
import static java.time.ZoneOffset.UTC;

final class JSONReaderUTF16CSV
        extends JSONReaderUTF16 {
    JSONReaderUTF16CSV(Context ctx, String str, char[] chars, int offset, int length) {
        super(ctx, str, chars, offset, length);
        super.csv = true;
    }

    @Override
    public boolean isArray() {
        return offset - 1 == start || chars[offset - 1] == '\n';
    }

    @Override
    public boolean isCSV() {
        return true;
    }

    public boolean isInt() {
        if (ch == '-' || ch == '+' || (ch >= '0' && ch <= '9')) {
            for (int i = offset; i < end; ++i) {
                char ch = chars[i];
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
                char ch = chars[i];
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
        this.ch = chars[offset++];
        return true;
    }

    public int getStringLength() {
        boolean quote = this.ch == '"';
        if (!quote) {
            int len = 1;
            for (int i = offset; i < end; ++i, len++) {
                char ch = chars[i];
                if (ch == ',' || ch == '\r' || ch == '\n') {
                    break;
                }
            }
            return len;
        }

        int len = 0;
        for (int i = offset; i < end; ++i, ++len) {
            char ch = chars[i];
            if (ch == '"') {
                if (i + 1 < end && chars[i + 1] == '"') {
                    ++i;
                    ++len;
                    continue;
                }
                break;
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
                char ch = chars[i];
                if (ch == ',' || ch == '\r' || ch == '\n') {
                    break;
                }
            }
            str = new String(chars, offset - 1, len + 1);
            offset += len;
        } else {
            int escapeCount = 0;
            for (int i = offset; i < end; ++i, ++len) {
                char ch = chars[i];
                if (ch == '"') {
                    if (i + 1 < end && chars[i + 1] == '"') {
                        ++i;
                        ++len;
                        escapeCount++;
                        continue;
                    }
                    break;
                }
            }

            if (escapeCount == 0) {
                str = new String(chars, offset, len);
                offset += len + 1;
            } else {
                char[] chars = new char[len - escapeCount];
                for (int i = offset, end = offset + len, index = 0; i < end; ++i) {
                    char ch = this.chars[i];
                    if (ch == '"') {
                        if (i + 1 < end && this.chars[i + 1] == '"') {
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

        ch = chars[offset++];
        return str;
    }

    @Override
    public LocalDate readLocalDate10() {
        int offset = this.offset;
        boolean quote = false;
        if (ch == '"') {
            quote = true;
            offset++;
        }

        char c0 = chars[offset - 1];
        char c1 = chars[offset];
        char c2 = chars[offset + 1];
        char c3 = chars[offset + 2];
        char c4 = chars[offset + 3];
        char c5 = chars[offset + 4];
        char c6 = chars[offset + 5];
        char c7 = chars[offset + 6];
        char c8 = chars[offset + 7];
        char c9 = chars[offset + 8];

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
        } else if (c4 == '年' && c6 == '月' && c9 == '日') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = '0';
            m1 = c5;

            d0 = c7;
            d1 = c8;
        } else if (c4 == '년' && c6 == '월' && c9 == '일') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = '0';
            m1 = c5;

            d0 = c7;
            d1 = c8;
        } else if (c4 == '年' && c7 == '月' && c9 == '日') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = '0';
            d1 = c8;
        } else if (c4 == '년' && c7 == '월' && c9 == '일') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = '0';
            d1 = c8;
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

        if (quote) {
            this.offset += 11;
        } else {
            this.offset += 9;
        }
        this.ch = chars[this.offset++];
        return ldt;
    }

    @Override
    protected LocalDateTime readLocalDateTime19() {
        char c0 = ch;
        char c1 = chars[offset];
        char c2 = chars[offset + 1];
        char c3 = chars[offset + 2];
        char c4 = chars[offset + 3];
        char c5 = chars[offset + 4];
        char c6 = chars[offset + 5];
        char c7 = chars[offset + 6];
        char c8 = chars[offset + 7];
        char c9 = chars[offset + 8];
        char c10 = chars[offset + 9];
        char c11 = chars[offset + 10];
        char c12 = chars[offset + 11];
        char c13 = chars[offset + 12];
        char c14 = chars[offset + 13];
        char c15 = chars[offset + 14];
        char c16 = chars[offset + 15];
        char c17 = chars[offset + 16];
        char c18 = chars[offset + 17];

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
        this.ch = chars[offset++];
        return ldt;
    }

    @Override
    protected ZonedDateTime readZonedDateTimeX(int len) {
        if (ch == '"') {
            return super.readZonedDateTimeX(len);
        }

        if (len < 19) {
            return null;
        }

        char c0 = ch;
        char c1 = chars[offset];
        char c2 = chars[offset + 1];
        char c3 = chars[offset + 2];
        char c4 = chars[offset + 3];
        char c5 = chars[offset + 4];
        char c6 = chars[offset + 5];
        char c7 = chars[offset + 6];
        char c8 = chars[offset + 7];
        char c9 = chars[offset + 8];
        char c10 = chars[offset + 9];
        char c11 = chars[offset + 10];
        char c12 = chars[offset + 11];
        char c13 = chars[offset + 12];
        char c14 = chars[offset + 13];
        char c15 = chars[offset + 14];
        char c16 = chars[offset + 15];
        char c17 = chars[offset + 16];
        char c18 = chars[offset + 17];
        char c19 = len == 19 ? ' ' : chars[offset + 18];

        char c20, c21 = '0', c22 = '0', c23 = '0', c24 = '0', c25 = '0', c26 = '0', c27 = '0', c28 = '0', c29 = '\0';
        switch (len) {
            case 19:
                c20 = '\0';
                break;
            case 20:
                c20 = '\0';
                break;
            case 21:
                c20 = chars[offset + 19];
                break;
            case 22:
                c20 = chars[offset + 19];
                c21 = chars[offset + 20];
                break;
            case 23:
                c20 = chars[offset + 19];
                c21 = chars[offset + 20];
                c22 = chars[offset + 21];
                break;
            case 24:
                c20 = chars[offset + 19];
                c21 = chars[offset + 20];
                c22 = chars[offset + 21];
                c23 = chars[offset + 22];
                break;
            case 25:
                c20 = chars[offset + 19];
                c21 = chars[offset + 20];
                c22 = chars[offset + 21];
                c23 = chars[offset + 22];
                c24 = chars[offset + 23];
                break;
            case 26:
                c20 = chars[offset + 19];
                c21 = chars[offset + 20];
                c22 = chars[offset + 21];
                c23 = chars[offset + 22];
                c24 = chars[offset + 23];
                c25 = chars[offset + 24];
                break;
            case 27:
                c20 = chars[offset + 19];
                c21 = chars[offset + 20];
                c22 = chars[offset + 21];
                c23 = chars[offset + 22];
                c24 = chars[offset + 23];
                c25 = chars[offset + 24];
                c26 = chars[offset + 25];
                break;
            case 28:
                c20 = chars[offset + 19];
                c21 = chars[offset + 20];
                c22 = chars[offset + 21];
                c23 = chars[offset + 22];
                c24 = chars[offset + 23];
                c25 = chars[offset + 24];
                c26 = chars[offset + 25];
                c27 = chars[offset + 26];
                break;
            case 29:
                c20 = chars[offset + 19];
                c21 = chars[offset + 20];
                c22 = chars[offset + 21];
                c23 = chars[offset + 22];
                c24 = chars[offset + 23];
                c25 = chars[offset + 24];
                c26 = chars[offset + 25];
                c27 = chars[offset + 26];
                c28 = chars[offset + 27];
                break;
            default:
                c20 = chars[offset + 19];
                c21 = chars[offset + 20];
                c22 = chars[offset + 21];
                c23 = chars[offset + 22];
                c24 = chars[offset + 23];
                c25 = chars[offset + 24];
                c26 = chars[offset + 25];
                c27 = chars[offset + 26];
                c28 = chars[offset + 27];
                c29 = chars[offset + 28];
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

        char first = chars[this.offset + zoneIdBegin - 1];

        LocalDateTime ldt = localDateTime(y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2, S3, S4, S5, S6, S7, S8);

        ZoneId zoneId;
        if (isTimeZone) {
            String tzStr = new String(chars, this.offset + zoneIdBegin, len - zoneIdBegin - 2);
            TimeZone timeZone = TimeZone.getTimeZone(tzStr);
            zoneId = timeZone.toZoneId();
        } else {
            if (first == 'Z') {
                zoneId = UTC;
            } else {
                String zoneIdStr;
                if (first == '+' || first == '-') {
                    zoneIdStr = new String(chars, this.offset + zoneIdBegin - 1, len - zoneIdBegin);
                } else if (first == ' ') {
                    zoneIdStr = new String(chars, this.offset + zoneIdBegin, len - zoneIdBegin - 1);
                } else { // '[
                    if (zoneIdBegin < len) {
                        zoneIdStr = new String(chars, this.offset + zoneIdBegin, len - zoneIdBegin - 2);
                    } else {
                        zoneIdStr = null;
                    }
                }
                zoneId = DateUtils.getZoneId(zoneIdStr, context.zoneId);
            }
        }

        ZonedDateTime zdt = ZonedDateTime.ofLocal(ldt, zoneId, null);

        offset += len - 1;
        this.ch = chars[offset++];
        return zdt;
    }

    public void next() {
        this.ch = chars[offset++];
    }
}
