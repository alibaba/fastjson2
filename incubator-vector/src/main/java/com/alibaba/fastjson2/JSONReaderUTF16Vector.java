package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.TypeUtils;
import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.Vector;

import static com.alibaba.fastjson2.JSONWriterUTF16Vector.*;

final class JSONReaderUTF16Vector
        extends JSONReaderUTF16 {
    JSONReaderUTF16Vector(Context ctx, String str, char[] chars, int offset, int length) {
        super(ctx, str, chars, offset, length);
    }

    JSONReaderUTF16Vector(Context ctx, String str, int offset, int length) {
        super(ctx, str, offset, length);
    }

    @Override
    public String readString() {
        char[] chars = this.chars;
        if (ch == '"' || ch == '\'') {
            final char quote = ch;

            int offset = this.offset;
            final int start = offset, end = this.end;
            int valueLength;
            boolean valueEscape = false;

            {
                int i = 0;
                final Vector<Short> v_quote = quote == '"' ? V_SHORT_128_DOUBLE_QUOTE : V_SHORT_128_SINGLE_QUOTE;
                for (; offset + 8 < end; offset += 8, i += 8) {
                    ShortVector v = ShortVector.fromCharArray(ShortVector.SPECIES_128, chars, offset);
                    if (v.eq(V_SHORT_128_SLASH).or(v.eq(v_quote)).anyTrue()) {
                        break;
                    }
                }

                for (; ; ++i) {
                    if (offset >= end) {
                        throw new JSONException(info("invalid escape character EOI"));
                    }
                    char c = chars[offset];
                    if (c == '\\') {
                        valueEscape = true;
                        c = chars[offset + 1];
                        offset += (c == 'u' ? 6 : (c == 'x' ? 4 : 2));
                        continue;
                    }

                    if (c == quote) {
                        valueLength = i;
                        break;
                    }
                    offset++;
                }
            }

            String str;
            if (valueEscape) {
                char[] buf = new char[valueLength];
                offset = start;
                for (int i = 0; ; ++i) {
                    char c = chars[offset];
                    if (c == '\\') {
                        c = chars[++offset];
                        switch (c) {
                            case 'u': {
                                c = char4(chars[offset + 1], chars[offset + 2], chars[offset + 3], chars[offset + 4]);
                                offset += 4;
                                break;
                            }
                            case 'x': {
                                c = char2(chars[offset + 1], chars[offset + 2]);
                                offset += 2;
                                break;
                            }
                            case '\\':
                            case '"':
                                break;
                            default:
                                c = char1(c);
                                break;
                        }
                    } else if (c == quote) {
                        break;
                    }
                    buf[i] = c;
                    offset++;
                }

                str = new String(buf);
            } else {
                char c0, c1;
                int strlen = offset - start;
                if (strlen == 1 && (c0 = chars[start]) < 128) {
                    str = TypeUtils.toString(c0);
                } else if (strlen == 2
                        && (c0 = chars[start]) < 128
                        && (c1 = chars[start + 1]) < 128
                ) {
                    str = TypeUtils.toString(c0, c1);
                } else {
                    str = this.str.substring(start, offset);
                }
            }

            if ((context.features & Feature.TrimString.mask) != 0) {
                str = str.trim();
            }

            int ch = ++offset == end ? EOI : chars[offset++];
            while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }

            if (comma = ch == ',') {
                ch = offset == end ? EOI : chars[offset++];
                while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                    ch = offset == end ? EOI : chars[offset++];
                }
            }

            this.ch = (char) ch;
            this.offset = offset;
            return str;
        }

        return readStringNotMatch();
    }

    public static class Factory
            implements JSONFactory.JSONReaderUTF16Creator {
        @Override
        public JSONReader create(Context ctx, String str, char[] chars, int offset, int length) {
            if (chars == null) {
                return new JSONReaderUTF16Vector(ctx, str, offset, length);
            } else {
                return new JSONReaderUTF16Vector(ctx, str, chars, offset, length);
            }
        }
    }
}
