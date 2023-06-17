package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.TypeUtils;
import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.Vector;

import static com.alibaba.fastjson2.JSONWriterUTF16Vector.*;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_CREATOR_JDK8;

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
        if (ch == '"' || ch == '\'') {
            final char quote = ch;

            int offset = this.offset;
            int start = offset;
            int valueLength;
            boolean valueEscape = false;

            _for:
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
                        c = chars[++offset];
                        switch (c) {
                            case 'u': {
                                offset += 4;
                                break;
                            }
                            case 'x': {
                                offset += 2;
                                break;
                            }
                            default:
                                // skip
                                break;
                        }
                        offset++;
                        continue;
                    }

                    if (c == quote) {
                        valueLength = i;
                        break _for;
                    }
                    offset++;
                }
            }

            String str;
            if (valueEscape) {
                char[] chars = new char[valueLength];
                offset = start;
                for (int i = 0; ; ++i) {
                    char c = this.chars[offset];
                    if (c == '\\') {
                        c = this.chars[++offset];
                        switch (c) {
                            case 'u': {
                                char c1 = this.chars[1 + offset];
                                char c2 = this.chars[2 + offset];
                                char c3 = this.chars[3 + offset];
                                char c4 = this.chars[4 + offset];
                                offset += 4;
                                c = char4(c1, c2, c3, c4);
                                break;
                            }
                            case 'x': {
                                char c1 = this.chars[1 + offset];
                                char c2 = this.chars[2 + offset];
                                offset += 2;
                                c = char2(c1, c2);
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
                    chars[i] = c;
                    offset++;
                }

                if (STRING_CREATOR_JDK8 != null) {
                    str = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                } else {
                    str = new String(chars);
                }
            } else {
                char c0, c1;
                int strlen = offset - this.offset;
                if (strlen == 1 && (c0 = this.chars[this.offset]) < 128) {
                    str = TypeUtils.toString(c0);
                } else if (strlen == 2
                        && (c0 = this.chars[this.offset]) < 128
                        && (c1 = this.chars[this.offset + 1]) < 128
                ) {
                    str = TypeUtils.toString(c0, c1);
                } else {
                    str = this.str.substring(this.offset, offset);
                }
            }

            if ((context.features & Feature.TrimString.mask) != 0) {
                str = str.trim();
            }

            clear:
            if (++offset != end) {
                char e = chars[offset++];
                while (e <= ' ' && (1L << e & SPACE) != 0) {
                    if (offset == end) {
                        break clear;
                    } else {
                        e = chars[offset++];
                    }
                }

                if (comma = e == ',') {
                    if (offset == end) {
                        e = EOI;
                    } else {
                        e = chars[offset++];
                        while (e <= ' ' && (1L << e & SPACE) != 0) {
                            if (offset == end) {
                                e = EOI;
                                break;
                            } else {
                                e = chars[offset++];
                            }
                        }
                    }
                }

                this.ch = e;
                this.offset = offset;
                return str;
            }

            this.ch = EOI;
            this.comma = false;
            this.offset = offset;
            return str;
        }

        switch (ch) {
            case '[':
                return toString(
                        readArray());
            case '{':
                return toString(
                        readObject());
            case '-':
            case '+':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                readNumber0();
                Number number = getNumber();
                return number.toString();
            case 't':
            case 'f':
                boolValue = readBoolValue();
                return boolValue ? "true" : "false";
            case 'n': {
                readNull();
                return null;
            }
            default:
                throw new JSONException(info("illegal input : " + ch));
        }
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
