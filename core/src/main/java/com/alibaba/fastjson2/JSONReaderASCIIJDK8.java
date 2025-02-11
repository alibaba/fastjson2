package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.TypeUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.alibaba.fastjson2.JSONReaderJSONB.check3;
import static com.alibaba.fastjson2.util.IOUtils.getLongLE;
import static com.alibaba.fastjson2.util.IOUtils.hexDigit4;

final class JSONReaderASCIIJDK8 extends JSONReaderASCII {
    JSONReaderASCIIJDK8(Context ctx, String str, byte[] bytes, int offset, int length) {
        super(ctx, str, bytes, offset, length);
    }

    JSONReaderASCIIJDK8(Context ctx, InputStream is) {
        super(ctx, is);
    }

    @Override
    public String readString() {
        if (ch == '"' || ch == '\'') {
            final byte[] bytes = this.bytes;
            char quote = this.ch;
            int valueLength;
            int offset = this.offset;
            final int start = offset, end = this.end;
            final long byteVectorQuote = quote == '\'' ? 0x2727_2727_2727_2727L : 0x2222_2222_2222_2222L;
            valueEscape = false;

            {
                int i = 0;
                int upperBound = offset + ((end - offset) & ~7);
                while (offset < upperBound) {
                    long v = getLongLE(bytes, offset);
                    if ((v & 0xFF00FF00FF00FF00L) != 0 || containsSlashOrQuote(v, byteVectorQuote)) {
                        break;
                    }

                    offset += 8;
                    i += 8;
                }
                // ...

                for (; ; ++i) {
                    if (offset >= end) {
                        throw new JSONException("invalid escape character EOI");
                    }

                    int c = bytes[offset] & 0xFF;
                    if (c == '\\') {
                        valueEscape = true;
                        c = bytes[offset + 1];
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
                char[] chars = new char[valueLength];
                offset = start;
                for (int i = 0; ; ++i) {
                    int ch = bytes[offset];
                    if (ch == '\\') {
                        ch = bytes[++offset];
                        switch (ch) {
                            case 'u': {
                                ch = hexDigit4(bytes, check3(offset + 1, end));
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
                            case 'b':
                                ch = '\b';
                                break;
                            case 't':
                                ch = '\t';
                                break;
                            case 'n':
                                ch = '\n';
                                break;
                            case 'f':
                                ch = '\f';
                                break;
                            case 'r':
                                ch = '\r';
                                break;
                            default:
                                ch = char1(ch);
                                break;
                        }
                        chars[i] = (char) ch;
                        offset++;
                    } else if (ch == quote) {
                        break;
                    } else {
                        chars[i] = (char) ch;
                        offset++;
                    }
                }

                str = new String(chars);
            } else {
                int strlen = offset - start;
                if (strlen == 1) {
                    str = TypeUtils.toString((char) (bytes[start] & 0xff));
                } else if (strlen == 2) {
                    str = TypeUtils.toString(
                            (char) (bytes[start] & 0xff),
                            (char) (bytes[start + 1] & 0xff)
                    );
                } else {
                    str = new String(bytes, start, offset - start, StandardCharsets.US_ASCII);
                }
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

        return readStringNotMatch();
    }
}
