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
            final byte[] bytes = this.bytes;
            char quote = this.ch;
            int valueLength;
            int offset = this.offset;
            final int start = offset, end = this.end;
            boolean ascii = true;
            valueEscape = false;

            {
                int i = 0;
                Vector<Byte> v_quote = quote == '"' ? V_BYTE_64_DOUBLE_QUOTE : V_BYTE_64_SINGLE_QUOTE;
                for (; offset + 8 < end; offset += 8, i += 8) {
                    ByteVector v = (ByteVector) ByteVector.SPECIES_64.fromArray(bytes, offset);
                    if (v.eq(V_BYTE_64_SLASH).or(v.eq(v_quote).or(v.lt(V_BYTE_64_ZERO))).anyTrue()) {
                        break;
                    }
                }

                for (; ; ++i) {
                    if (offset >= end) {
                        throw new JSONException("invalid escape character EOI");
                    }

                    int c = bytes[offset];
                    if (c == '\\') {
                        valueEscape = true;
                        c = bytes[offset + 1];
                        offset += (c == 'u' ? 6 : (c == 'x' ? 4 : 2));
                        continue;
                    }

                    if (c >= 0) {
                        if (c == quote) {
                            valueLength = i;
                            break;
                        }
                        offset++;
                    } else {
                        ascii = false;
                        switch ((c & 0xFF) >> 4) {
                            case 12:
                            case 13: {
                                /* 110x xxxx   10xx xxxx*/
                                offset += 2;
                                break;
                            }
                            case 14: {
                                offset += 3;
                                break;
                            }
                            default: {
                                /* 10xx xxxx,  1111 xxxx */
                                if ((c >> 3) == -2) {
                                    offset += 4;
                                    i++;
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
                    int ch = bytes[offset];
                    if (ch == '\\') {
                        ch = bytes[++offset];
                        switch (ch) {
                            case 'u': {
                                ch = char4(bytes[offset + 1], bytes[offset + 2], bytes[offset + 3], bytes[offset + 4]);
                                offset += 4;
                                break;
                            }
                            case 'x': {
                                ch = char2(bytes[offset + 1], bytes[offset + 2]);
                                offset += 2;
                                break;
                            }
                            case '\\':
                            case '"':
                                break;
                            default:
                                ch = char1(ch);
                                break;
                        }
                        chars[i] = (char) ch;
                        offset++;
                    } else if (ch == '"') {
                        break;
                    } else {
                        if (ch >= 0) {
                            chars[i] = (char) ch;
                            offset++;
                        } else {
                            switch ((ch & 0xFF) >> 4) {
                                case 12:
                                case 13: {
                                    /* 110x xxxx   10xx xxxx*/
                                    chars[i] = (char) (((ch & 0x1F) << 6) | (bytes[offset + 1] & 0x3F));
                                    offset += 2;
                                    break;
                                }
                                case 14: {
                                    chars[i] = (char)
                                            (((ch & 0x0F) << 12) |
                                                    ((bytes[offset + 1] & 0x3F) << 6) |
                                                    ((bytes[offset + 2] & 0x3F) << 0));
                                    offset += 3;
                                    break;
                                }
                                default: {
                                    /* 10xx xxxx,  1111 xxxx */
                                    char2_utf8(bytes, offset, ch, chars, i);
                                    offset += 4;
                                    i++;
                                }
                            }
                        }
                    }
                }

                str = new String(chars);
            } else if (ascii) {
                int strlen = offset - start;
                if (strlen == 1) {
                    str = TypeUtils.toString((char) (bytes[start] & 0xff));
                } else if (strlen == 2) {
                    str = TypeUtils.toString(
                            (char) (bytes[start] & 0xff),
                            (char) (bytes[start + 1] & 0xff)
                    );
                } else if (STRING_CREATOR_JDK11 != null) {
                    str = STRING_CREATOR_JDK11.apply(
                            Arrays.copyOfRange(this.bytes, this.offset, offset),
                            LATIN1);
                } else {
                    str = new String(bytes, start, offset - start, StandardCharsets.US_ASCII);
                }
            } else {
                str = new String(bytes, start, offset - start, StandardCharsets.UTF_8);
            }

            if ((context.features & Feature.TrimString.mask) != 0) {
                str = str.trim();
            }

            int ch = ++offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if (comma = ch == ',') {
                ch = offset == end ? EOI : bytes[offset++];
                while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                    ch = offset == end ? EOI : bytes[offset++];
                }
            }

            this.ch = (char) ch;
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
