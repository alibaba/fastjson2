package com.alibaba.fastjson2;

import static com.alibaba.fastjson2.util.JDKUtils.*;

final class JSONWriterUTF8JDK9
        extends JSONWriterUTF8 {
    JSONWriterUTF8JDK9(Context ctx) {
        super(ctx);
    }

    @Override
    public void writeString(String str) {
        if (STRING_VALUE == null) {
            super.writeString(str);
            return;
        }

        if (str == null) {
            if (isEnabled(Feature.NullAsDefaultValue.mask | Feature.WriteNullStringAsEmpty.mask)) {
                writeString("");
                return;
            }

            writeNull();
            return;
        }

        int coder = STRING_CODER.applyAsInt(str);
        byte[] value = STRING_VALUE.apply(str);

        if (coder == 0) {
            writeStringLatin1(value);
            return;
            // end of latin
        }

        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;

        int minCapacity = off + value.length * 4 + 2;
        if (escapeNoneAscii) {
            minCapacity += value.length * 2;
        }

        if (minCapacity - this.bytes.length > 0) {
            ensureCapacity(minCapacity);
        }

        bytes[off++] = (byte) quote;

        int valueOffset = 0;
        while (valueOffset < value.length) {
            byte b0 = value[valueOffset++];
            byte b1 = value[valueOffset++];

            if (b1 == 0 && b0 >= 0) {
//                bytes[off++] = b0;
                byte ch = b0;
                switch (ch) {
                    case '\\':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) '\\';
                        break;
                    case '\n':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) 'n';
                        break;
                    case '\r':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) 'r';
                        break;
                    case '\f':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) 'f';
                        break;
                    case '\b':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) 'b';
                        break;
                    case '\t':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) 't';
                        break;
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        bytes[off++] = '\\';
                        bytes[off++] = 'u';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = (byte) ('0' + (int) ch);
                        break;
                    case 11:
                    case 14:
                    case 15:
                        bytes[off++] = '\\';
                        bytes[off++] = 'u';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = (byte) ('a' + (ch - 10));
                        break;
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                    case 24:
                    case 25:
                        bytes[off++] = '\\';
                        bytes[off++] = 'u';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = '1';
                        bytes[off++] = (byte) ('0' + (ch - 16));
                        break;
                    case 26:
                    case 27:
                    case 28:
                    case 29:
                    case 30:
                    case 31:
                        bytes[off++] = '\\';
                        bytes[off++] = 'u';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = '1';
                        bytes[off++] = (byte) ('a' + (ch - 26));
                        break;
                    default:
                        if (ch == quote) {
                            bytes[off++] = (byte) '\\';
                            bytes[off++] = (byte) quote;
                        } else {
                            bytes[off++] = ch;
                        }
                        break;
                }
            } else {
                char c = (char) (((b0 & 0xff) << 0) | ((b1 & 0xff) << 8));
                if (c < 0x800) {
                    // 2 bytes, 11 bits
                    bytes[off++] = (byte) (0xc0 | (c >> 6));
                    bytes[off++] = (byte) (0x80 | (c & 0x3f));
                } else if (escapeNoneAscii) {
                    bytes[off++] = '\\';
                    bytes[off++] = 'u';
                    bytes[off++] = (byte) DIGITS[(c >>> 12) & 15];
                    bytes[off++] = (byte) DIGITS[(c >>> 8) & 15];
                    bytes[off++] = (byte) DIGITS[(c >>> 4) & 15];
                    bytes[off++] = (byte) DIGITS[c & 15];
                } else if (c >= '\uD800' && c < ('\uDFFF' + 1)) { //Character.isSurrogate(c) but 1.7
                    final int uc;
                    int ip = valueOffset - 1;
                    if (c >= '\uD800' && c < ('\uDBFF' + 1)) { // Character.isHighSurrogate(c)
                        if (value.length - ip < 2) {
                            uc = -1;
                        } else {
                            b0 = value[ip + 1];
                            b1 = value[ip + 2];
                            char d = (char) (((b0 & 0xff) << 0) | ((b1 & 0xff) << 8));
                            // d >= '\uDC00' && d < ('\uDFFF' + 1)
                            if (d >= '\uDC00' && d < ('\uDFFF' + 1)) { // Character.isLowSurrogate(d)
                                valueOffset += 2;
                                uc = ((c << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00'); // Character.toCodePoint(c, d)
                            } else {
                                bytes[off++] = '?';
                                continue;
                            }
                        }
                    } else {
                        //
                        if (c >= '\uDC00' && c < ('\uDFFF' + 1)) { // Character.isLowSurrogate(c)
                            bytes[off++] = '?';
                            continue;
                        } else {
                            uc = c;
                        }
                    }

                    if (uc < 0) {
                        bytes[off++] = (byte) '?';
                    } else {
                        bytes[off++] = (byte) (0xf0 | ((uc >> 18)));
                        bytes[off++] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                        bytes[off++] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                        bytes[off++] = (byte) (0x80 | (uc & 0x3f));
                    }
                } else {
                    // 3 bytes, 16 bits
                    bytes[off++] = (byte) (0xe0 | ((c >> 12)));
                    bytes[off++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                    bytes[off++] = (byte) (0x80 | (c & 0x3f));
                }
            }
        }

        bytes[off++] = (byte) quote;
    }
}
