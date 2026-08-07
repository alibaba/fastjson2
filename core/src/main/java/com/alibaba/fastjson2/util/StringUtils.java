package com.alibaba.fastjson2.util;

import java.nio.charset.StandardCharsets;

import static com.alibaba.fastjson2.JSONWriter.Feature.BrowserSecure;
import static com.alibaba.fastjson2.JSONWriter.Feature.EscapeNoneAscii;
import static com.alibaba.fastjson2.util.IOUtils.*;
import static com.alibaba.fastjson2.util.IOUtils.hex4U;
import static com.alibaba.fastjson2.util.JDKUtils.ARRAY_BYTE_BASE_OFFSET;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

public class StringUtils {
    protected static final long MASK_ESCAPE_NONE_ASCII = EscapeNoneAscii.mask;
    protected static final long MASK_BROWSER_SECURE = BrowserSecure.mask;
    public static int writeLatin1(byte[] bytes, int off, byte[] value, byte quote) {
        int strlen = value.length;
        bytes[off] = quote;
        System.arraycopy(value, 0, bytes, off + 1, strlen);
        bytes[off + strlen + 1] = quote;
        return off + strlen + 2;
    }

    public static int writeLatin1Escaped(byte[] bytes, int off, byte[] values, byte quote, long features) {
        final boolean browserSecure = (features & MASK_BROWSER_SECURE) != 0;
        bytes[off++] = quote;
        for (int i = 0; i < values.length; i++) {
            byte ch = values[i];
            switch (ch) {
                case '\\':
                case '\n':
                case '\r':
                case '\f':
                case '\b':
                case '\t':
                    writeEscapedChar(bytes, off, ch);
                    off += 2;
                    break;
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 11:
                case 14:
                case 15:
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
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    writeU4Hex2(bytes, off, ch);
                    off += 6;
                    break;
                case '<':
                case '>':
                case '(':
                case ')':
                    if (browserSecure) {
                        writeU4HexU(bytes, off, ch);
                        off += 6;
                    } else {
                        bytes[off++] = ch;
                    }
                    break;
                default:
                    if (ch == quote) {
                        bytes[off] = '\\';
                        bytes[off + 1] = quote;
                        off += 2;
                    } else if (ch < 0) {
                        // latin
                        int c = ch & 0xFF;
                        bytes[off] = (byte) (0xc0 | (c >> 6));
                        bytes[off + 1] = (byte) (0x80 | (c & 0x3f));
                        off += 2;
                    } else {
                        bytes[off++] = ch;
                    }
                    break;
            }
        }
        bytes[off] = quote;
        return off + 1;
    }

    public static int writeLatin1EscapedRest(char[] chars, int off, byte[] str, int coff, char quote, long features) {
        boolean escapeNoneAscii = (features & EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (features & BrowserSecure.mask) != 0;

        for (int i = coff; i < str.length; i++) {
            byte b = str[i];
            char ch = (char) (b & 0xff);
            switch (ch) {
                case '"':
                case '\'':
                    if (ch == quote) {
                        chars[off++] = '\\';
                    }
                    chars[off++] = ch;
                    break;
                case '\\':
                case '\r':
                case '\n':
                case '\b':
                case '\f':
                case '\t':
                    writeEscapedChar(chars, off, ch);
                    off += 2;
                    break;
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 11:
                case 14:
                case 15:
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
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    writeU4Hex2(chars, off, ch);
                    off += 6;
                    break;
                case '<':
                case '>':
                case '(':
                case ')':
                    if (browserSecure) {
                        writeU4HexU(chars, off, ch);
                        off += 6;
                    } else {
                        chars[off++] = ch;
                    }
                    break;
                default:
                    if (escapeNoneAscii && ch > 0x007F) {
                        writeU4HexU(chars, off, ch);
                        off += 6;
                    } else {
                        chars[off++] = ch;
                    }
                    break;
            }
        }
        chars[off] = quote;
        return off + 1;
    }

    public static int writeUTF16(byte[] bytes, int off, byte[] value, byte quote, long features) {
        boolean escapeNoneAscii = (features & MASK_ESCAPE_NONE_ASCII) != 0;
        boolean browserSecure = (features & MASK_BROWSER_SECURE) != 0;

        bytes[off++] = quote;

        int coff = 0, char_len = value.length >> 1;
        while (coff < char_len) {
            char c = IOUtils.getChar(value, coff++);
            if (c < 0x80) {
                switch (c) {
                    case '\\':
                    case '\n':
                    case '\r':
                    case '\f':
                    case '\b':
                    case '\t':
                        writeEscapedChar(bytes, off, c);
                        off += 2;
                        break;
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 11:
                    case 14:
                    case 15:
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
                    case 26:
                    case 27:
                    case 28:
                    case 29:
                    case 30:
                    case 31:
                        writeU4Hex2(bytes, off, c);
                        off += 6;
                        break;
                    case '<':
                    case '>':
                    case '(':
                    case ')':
                        if (browserSecure) {
                            writeU4HexU(bytes, off, c);
                            off += 6;
                        } else {
                            bytes[off++] = (byte) c;
                        }
                        break;
                    default:
                        if (c == quote) {
                            bytes[off] = '\\';
                            bytes[off + 1] = quote;
                            off += 2;
                        } else {
                            bytes[off++] = (byte) c;
                        }
                        break;
                }
            } else {
                if (c < 0x800) {
                    // 2 bytes, 11 bits
                    bytes[off] = (byte) (0xc0 | (c >> 6));
                    bytes[off + 1] = (byte) (0x80 | (c & 0x3f));
                    off += 2;
                } else if (escapeNoneAscii) {
                    writeU4HexU(bytes, off, c);
                    off += 6;
                } else if (c >= '\uD800' && c < ('\uDFFF' + 1)) { //Character.isSurrogate(c) but 1.7
                    final int uc;
                    if (c < '\uDBFF' + 1) { // Character.isHighSurrogate(c)
                        if (coff + 1 > char_len) {
                            uc = -1;
                        } else {
                            char d = getChar(value, coff);
                            // d >= '\uDC00' && d < ('\uDFFF' + 1)
                            if (d >= '\uDC00' && d < ('\uDFFF' + 1)) { // Character.isLowSurrogate(d)
                                coff++;
                                uc = ((c << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00'); // Character.toCodePoint(c, d)
                            } else {
                                bytes[off++] = (byte) '?';
                                continue;
                            }
                        }
                    } else {
                        //
                        // Character.isLowSurrogate(c)
                        bytes[off++] = (byte) '?';
                        continue;
                    }

                    if (uc < 0) {
                        bytes[off++] = (byte) '?';
                    } else {
                        bytes[off] = (byte) (0xf0 | ((uc >> 18)));
                        bytes[off + 1] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                        bytes[off + 2] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                        bytes[off + 3] = (byte) (0x80 | (uc & 0x3f));
                        off += 4;
                    }
                } else {
                    // 3 bytes, 16 bits
                    bytes[off] = (byte) (0xe0 | ((c >> 12)));
                    bytes[off + 1] = (byte) (0x80 | ((c >> 6) & 0x3f));
                    bytes[off + 2] = (byte) (0x80 | (c & 0x3f));
                    off += 3;
                }
            }
        }

        bytes[off] = quote;
        return off + 1;
    }

    public static void writeEscapedChar(byte[] bytes, int off, int c0) {
        putShortLE(bytes, off, LATIN1.ESCAPED_CHARS[c0 & 0x7f]);
    }

    public static void writeU4Hex2(byte[] bytes, int off, int c) {
        putIntUnaligned(bytes, off, LATIN1.U4);
        putShortLE(bytes, off + 4, hex2(c));
    }

    public static void writeU4HexU(byte[] bytes, int off, int c) {
        putShortUnaligned(bytes, off, LATIN1.U2);
        putIntLE(bytes, off + 2, hex4U(c));
    }

    public static void writeEscapedChar(char[] chars, int off, int c0) {
        IOUtils.putIntUnaligned(chars, off, UTF16.ESCAPED_CHARS[c0 & 0x7f]);
    }

    public static void writeU4Hex2(char[] chars, int off, int c) {
        IOUtils.putLongUnaligned(chars, off, UTF16.U4);
        IOUtils.putIntLE(chars, off + 4, utf16Hex2(c));
    }

    public static void writeU4HexU(char[] chars, int off, int c) {
        IOUtils.putIntUnaligned(chars, off, UTF16.U2);
        IOUtils.putLongLE(chars, off + 2, utf16Hex4U(c));
    }

    public static boolean escaped(byte[] value, byte quote, long vecQuote) {
        int i = 0;
        final int upperBound = (value.length - i) & ~7;
        for (; i < upperBound; i += 8) {
            if (!noneEscaped(getLongUnaligned(value, i), vecQuote)) {
                return true;
            }
        }
        for (; i < value.length; i++) {
            byte c = value[i];
            if (c == quote || c == '\\' || c < ' ') {
                return true;
            }
        }
        return false;
    }

    public static boolean noneEscaped(long v, long quote) {
        /*
          for (int i = 0; i < 8; ++i) {
            byte c = (byte) data;
            if (c == (byte) quote || c == '\\' || c < ' ') {
                return false;
            }
            data >>>= 8;
          }
          return true;
         */
        return ((v + 0x6060606060606060L) & 0x8080808080808080L) == 0x8080808080808080L // all >= 32
                && ((v ^ quote) + 0x0101010101010101L & 0x8080808080808080L) == 0x8080808080808080L // != quote
                && ((v ^ 0xA3A3A3A3A3A3A3A3L) + 0x0101010101010101L & 0x8080808080808080L) == 0x8080808080808080L; // != '\\'
    }

    public static final class LATIN1 {
        private static final short U2;
        private static final int U4;
        private static final short[] ESCAPED_CHARS;

        static {
            {
                byte[] bytes = "\\u00".getBytes(StandardCharsets.UTF_8);
                U2 = UNSAFE.getShort(bytes, ARRAY_BYTE_BASE_OFFSET);
                U4 = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET);
            }
            {
                char slash = '\\';
                short[] shorts = new short[128];
                shorts['\\'] = (short) (slash | ('\\' << 8));
                shorts['\n'] = (short) (slash | ('n' << 8));
                shorts['\r'] = (short) (slash | ('r' << 8));
                shorts['\f'] = (short) (slash | ('f' << 8));
                shorts['\b'] = (short) (slash | ('b' << 8));
                shorts['\t'] = (short) (slash | ('t' << 8));
                ESCAPED_CHARS = shorts;
            }
        }
    }

    public static final class UTF16 {
        private static final int U2;
        private static final long U4;
        private static final int[] ESCAPED_CHARS;

        static {
            {
                char[] bytes = "\\u00".toCharArray();
                U2 = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET);
                U4 = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET);
            }
            {
                char[] mapping = new char[]{
                        '\\', '\\',
                        '\n', 'n',
                        '\r', 'r',
                        '\f', 'f',
                        '\b', 'b',
                        '\t', 't'
                };
                char[] buf = {'\\', '\0'};
                int[] shorts = new int[128];
                for (int i = 0; i < mapping.length; i += 2) {
                    buf[1] = mapping[i + 1];
                    shorts[mapping[i]] = IOUtils.getIntUnaligned(buf, 0);
                }
                ESCAPED_CHARS = shorts;
            }
        }
    }
}
