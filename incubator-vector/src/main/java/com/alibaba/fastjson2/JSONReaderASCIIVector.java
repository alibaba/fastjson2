package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.TypeUtils;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.Vector;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONWriterUTF16Vector.*;
import static com.alibaba.fastjson2.util.JDKUtils.LATIN1;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_CREATOR_JDK11;

final class JSONReaderASCIIVector
        extends JSONReaderASCII {
    public JSONReaderASCIIVector(Context ctx, String str, byte[] bytes, int offset, int length) {
        super(ctx, str, bytes, offset, length);
    }

    @Override
    public String readString() {
        if (ch == '"' || ch == '\'') {
            final byte quote = (byte) ch;
            final byte slash = (byte) '\\';

            final byte[] bytes = this.bytes;
            int offset = this.offset;
            final int start = offset;
            int valueLength;
            boolean valueEscape = false;

            _for:
            {
                int i = 0;
                Vector<Byte> v_quote = quote == '"' ? V_BYTE_64_DOUBLE_QUOTE : V_BYTE_64_SINGLE_QUOTE;
                for (; offset + 8 < end; offset += 8, i += 8) {
                    ByteVector v = (ByteVector) ByteVector.SPECIES_64.fromArray(bytes, offset);
                    if (v.eq(V_BYTE_64_SLASH).or(v.eq(v_quote)).anyTrue()) {
                        break;
                    }
                }

                for (; ; ++i) {
                    if (offset >= end) {
                        throw new JSONException("invalid escape character EOI");
                    }

                    byte c = bytes[offset];
                    if (c == slash) {
                        valueEscape = true;
                        c = bytes[offset + 1];
                        offset += (c == 'u' ? 6 : (c == 'x' ? 4 : 2));
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
                    int c = bytes[offset] & 0xff;
                    if (c == '\\') {
                        c = bytes[++offset] & 0xff;
                        switch (c) {
                            case 'u': {
                                c = char4(bytes[offset + 1], bytes[offset + 2], bytes[offset + 3], bytes[offset + 4]);
                                offset += 4;
                                break;
                            }
                            case 'x': {
                                c = char2(bytes[offset + 1], bytes[offset + 2]);
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
                    chars[i] = (char) c;
                    offset++;
                }

                str = new String(chars);
            } else {
                int strlen = offset - start;
                if (strlen == 1) {
                    str = TypeUtils.toString(bytes[start]);
                } else if (strlen == 2) {
                    str = TypeUtils.toString(
                            bytes[start],
                            bytes[start + 1]
                    );
                } else if (this.str != null) {
                    str = this.str.substring(start, offset);
                } else if (STRING_CREATOR_JDK11 != null) {
                    byte[] buf = Arrays.copyOfRange(bytes, start, offset);
                    str = STRING_CREATOR_JDK11.apply(buf, LATIN1);
                } else {
                    str = new String(bytes, start, offset - start, StandardCharsets.ISO_8859_1);
                }
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
            return new JSONReaderASCIIVector(ctx, str, bytes, offset, length);
        }
    }
}
