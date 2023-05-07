package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.TypeUtils;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.Vector;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONWriterUTF16Vector.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;

final class JSONReaderUTF8Vector
        extends JSONReaderUTF8 {
    JSONReaderUTF8Vector(Context ctx, String str, byte[] bytes, int offset, int length) {
        super(ctx, str, bytes, offset, length);
    }

    @Override
    public String readString() {
        if (ch == '"' || ch == '\'') {
            char quote = this.ch;
            int valueLength;
            int offset = this.offset;
            int start = offset;
            boolean ascii = true;
            valueEscape = false;

            {
                int i = 0;
                Vector<Byte> v_quote = quote == '"' ? V_BYTE_64_DOUBLE_QUOTE : V_BYTE_64_SINGLE_QUOTE;
                for (; offset + 8 < end; offset += 8, i += 8) {
                    ByteVector v = (ByteVector) ByteVector.SPECIES_64.fromArray(bytes, offset);
                    if (v.eq(V_BYTE_64_SLASH).or(v.eq(v_quote)).anyTrue()) {
                        break;
                    }
                }

                _for:
                for (; ; ++i) {
                    if (offset >= end) {
                        throw new JSONException("invalid escape character EOI");
                    }

                    int c = bytes[offset];
                    if (c == '\\') {
                        valueEscape = true;
                        c = bytes[++offset];
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
                                break;
                        }
                        offset++;
                        continue;
                    }

                    if (c >= 0) {
                        if (c == quote) {
                            valueLength = i;
                            break _for;
                        }
                        offset++;
                    } else {
                        switch ((c & 0xFF) >> 4) {
                            case 12:
                            case 13: {
                                /* 110x xxxx   10xx xxxx*/
                                offset += 2;
                                ascii = false;
                                break;
                            }
                            case 14: {
                                offset += 3;
                                ascii = false;
                                break;
                            }
                            default: {
                                /* 10xx xxxx,  1111 xxxx */
                                if ((c >> 3) == -2) {
                                    offset += 4;
                                    i++;
                                    ascii = false;
                                    break;
                                }

                                throw new JSONException("malformed input around byte " + offset);
                            }
                        }
                    }
                }
            }

            String str;
            if (valueEscape) {
                char[] chars = new char[valueLength];
                offset = start;
                for (int i = 0; ; ++i) {
                    int c = bytes[offset];
                    if (c == '\\') {
                        c = bytes[++offset];
                        switch (c) {
                            case 'u': {
                                int c1 = bytes[++offset];
                                int c2 = bytes[++offset];
                                int c3 = bytes[++offset];
                                int c4 = bytes[++offset];
                                c = char4(c1, c2, c3, c4);
                                break;
                            }
                            case 'x': {
                                int c1 = bytes[++offset];
                                int c2 = bytes[++offset];
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
                        chars[i] = (char) c;
                        offset++;
                    } else if (c == '"') {
                        break;
                    } else {
                        if (c >= 0) {
                            chars[i] = (char) c;
                            offset++;
                        } else {
                            switch ((c & 0xFF) >> 4) {
                                case 12:
                                case 13: {
                                    /* 110x xxxx   10xx xxxx*/
                                    offset++;
                                    int c2 = bytes[offset++];
                                    chars[i] = (char) (
                                            ((c & 0x1F) << 6) | (c2 & 0x3F));
                                    break;
                                }
                                case 14: {
                                    offset++;
                                    int c2 = bytes[offset++];
                                    int c3 = bytes[offset++];
                                    chars[i] = (char)
                                            (((c & 0x0F) << 12) |
                                                    ((c2 & 0x3F) << 6) |
                                                    ((c3 & 0x3F) << 0));
                                    break;
                                }
                                default: {
                                    /* 10xx xxxx,  1111 xxxx */
                                    if ((c >> 3) == -2) {
                                        offset++;
                                        int c2 = bytes[offset++];
                                        int c3 = bytes[offset++];
                                        int c4 = bytes[offset++];
                                        int uc = ((c << 18) ^
                                                (c2 << 12) ^
                                                (c3 << 6) ^
                                                (c4 ^ (((byte) 0xF0 << 18) ^
                                                        ((byte) 0x80 << 12) ^
                                                        ((byte) 0x80 << 6) ^
                                                        ((byte) 0x80 << 0))));

                                        if (((c2 & 0xc0) != 0x80 || (c3 & 0xc0) != 0x80 || (c4 & 0xc0) != 0x80) // isMalformed4
                                                ||
                                                // shortest form check
                                                !(uc >= 0x010000 && uc < 0X10FFFF + 1) // !Character.isSupplementaryCodePoint(uc)
                                        ) {
                                            throw new JSONException("malformed input around byte " + offset);
                                        } else {
                                            chars[i++] = (char) ((uc >>> 10) + ('\uD800' - (0x010000 >>> 10))); // Character.highSurrogate(uc);
                                            chars[i] = (char) ((uc & 0x3ff) + '\uDC00'); // Character.lowSurrogate(uc);
                                        }
                                        break;
                                    }

                                    throw new JSONException("malformed input around byte " + offset);
                                }
                            }
                        }
                    }
                }

                str = new String(chars);
            } else if (ascii) {
                int strlen = offset - this.offset;
                if (strlen == 1) {
                    str = TypeUtils.toString((char) (bytes[this.offset] & 0xff));
                } else if (strlen == 2) {
                    str = TypeUtils.toString(
                            (char) (bytes[this.offset] & 0xff),
                            (char) (bytes[this.offset + 1] & 0xff)
                    );
                } else if (STRING_CREATOR_JDK8 != null) {
                    char[] chars = new char[strlen];
                    for (int i = 0; i < strlen; ++i) {
                        chars[i] = (char) bytes[this.offset + i];
                    }

                    str = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                } else if (STRING_CREATOR_JDK11 != null) {
                    byte[] bytes = Arrays.copyOfRange(this.bytes, this.offset, offset);
                    str = STRING_CREATOR_JDK11.apply(bytes, LATIN1);
                } else {
                    str = new String(bytes, this.offset, offset - this.offset, StandardCharsets.US_ASCII);
                }
            } else {
                str = new String(bytes, this.offset, offset - this.offset, StandardCharsets.UTF_8);
            }

            if ((context.features & Feature.TrimString.mask) != 0) {
                str = str.trim();
            }

            clear:
            if (++offset != end) {
                byte e = bytes[offset++];
                while (e <= ' ' && (1L << e & SPACE) != 0) {
                    if (offset == end) {
                        break clear;
                    } else {
                        e = bytes[offset++];
                    }
                }

                if (comma = e == ',') {
                    if (offset == end) {
                        e = EOI;
                    } else {
                        e = bytes[offset++];
                        while (e <= ' ' && (1L << e & SPACE) != 0) {
                            if (offset == end) {
                                e = EOI;
                                break;
                            } else {
                                e = bytes[offset++];
                            }
                        }
                    }
                }

                this.ch = (char) e;
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
                throw new JSONException("TODO : " + ch);
        }
    }

    public static class Factory
            implements JSONFactory.JSONReaderUTF8Creator {
        @Override
        public JSONReader create(Context ctx, String str, byte[] bytes, int offset, int length) {
            return new JSONReaderUTF8Vector(ctx, str, bytes, offset, length);
        }
    }
}
