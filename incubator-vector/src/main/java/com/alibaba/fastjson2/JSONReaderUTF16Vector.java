package com.alibaba.fastjson2;

import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.Vector;

import static com.alibaba.fastjson2.JSONWriterUTF16Vector.*;
import static com.alibaba.fastjson2.util.JDKUtils.JVM_VERSION;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_CREATOR_JDK8;

final class JSONReaderUTF16Vector
        extends JSONReaderUTF16 {
    JSONReaderUTF16Vector(Context ctx, String str, char[] chars, int offset, int length) {
        super(ctx, str, chars, offset, length);
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
                char c0 = 0, c1 = 0, c2 = 0, c3 = 0;

                // vector optimize
                boolean quoted = false;
                final Vector<Short> v_quote = quote == '"' ? V_SHORT_128_DOUBLE_QUOTE : V_SHORT_128_SINGLE_QUOTE;
                for (; offset + 8 < end; offset += 8, i += 8) {
                    ShortVector v = ShortVector.fromCharArray(ShortVector.SPECIES_128, chars, offset);
                    if (v.eq(V_SHORT_128_SLASH).or(v.eq(v_quote)).anyTrue()) {
                        break;
                    }
                }

                if (quoted) {
                    if (c0 == quote) {
                        // skip
                    } else if (c1 == quote) {
                        offset++;
                        i++;
                    } else if (c2 == quote) {
                        offset += 2;
                        i += 2;
                    } else if (c3 == quote) {
                        offset += 3;
                        i += 3;
                    }
                    valueLength = i;
                } else {
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
                                char c1 = this.chars[++offset];
                                char c2 = this.chars[++offset];
                                char c3 = this.chars[++offset];
                                char c4 = this.chars[++offset];
                                c = char4(c1, c2, c3, c4);
                                break;
                            }
                            case 'x': {
                                char c1 = this.chars[++offset];
                                char c2 = this.chars[++offset];
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
                if (this.str != null && JVM_VERSION > 8) {
                    str = this.str.substring(this.offset, offset);
                } else {
                    str = new String(chars, this.offset, offset - this.offset);
                }
            }

            if ((context.features & Feature.TrimString.mask) != 0) {
                str = str.trim();
            }

            if (offset + 1 == end) {
                this.offset = end;
                this.ch = EOI;
                comma = false;
                return str;
            }

            int b = chars[++offset];
            while (b <= ' ' && ((1L << b) & SPACE) != 0) {
                b = chars[++offset];
            }

            if (comma = (b == ',')) {
                this.offset = offset + 1;

                // inline next
                ch = this.offset == end ? EOI : chars[this.offset++];

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (this.offset >= end) {
                        ch = EOI;
                    } else {
                        ch = chars[this.offset++];
                    }
                }
            } else {
                this.offset = offset + 1;
                this.ch = (char) b;
            }

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
                throw new JSONException("TODO : " + ch);
        }
    }

    public static class Factory
            implements JSONFactory.JSONReaderUTF16Creator {
        @Override
        public JSONReader create(Context ctx, String str, char[] chars, int offset, int length) {
            return new JSONReaderUTF16Vector(ctx, str, chars, offset, length);
        }
    }
}
