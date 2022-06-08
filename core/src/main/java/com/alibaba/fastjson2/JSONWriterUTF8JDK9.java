package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONFactory.Utils.*;

final class JSONWriterUTF8JDK9
        extends JSONWriterUTF8 {
    JSONWriterUTF8JDK9(Context ctx) {
        super(ctx);
    }

    @Override
    public void writeString(String str) {
        if (str == null) {
            if (isEnabled(Feature.NullAsDefaultValue.mask | Feature.WriteNullStringAsEmpty.mask)) {
                writeString("");
                return;
            }

            writeNull();
            return;
        }

        byte[] value = null;
        int coder = 1;

        if (JDKUtils.UNSAFE_SUPPORT) {
            coder = UnsafeUtils.getStringCoder(str);
            if (coder == 0) {
                value = UnsafeUtils.getStringValue(str);
            }
        }

        if (value == null) {
            value = str.getBytes(StandardCharsets.UTF_8);
        }
        {
            int minCapacity = off
                    + value.length * 3 // utf8 3 bytes
                    + 2;

            if (minCapacity - this.bytes.length > 0) {
                int oldCapacity = this.bytes.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                this.bytes = Arrays.copyOf(this.bytes, newCapacity);
            }
        }
        bytes[off++] = (byte) quote;
        boolean special = false;
        {
            int i = 0;
            // vector optimize
            while (i + 4 <= value.length) {
                byte c0 = value[i];
                byte c1 = value[i + 1];
                byte c2 = value[i + 2];
                byte c3 = value[i + 3];
                if (c0 == quote || c1 == quote || c2 == quote || c3 == quote
                        || c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\'
                        || c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' '
                ) {
                    special = true;
                    break;
                }
                i += 4;
            }
            if (!special && i + 2 <= value.length) {
                byte c0 = value[i];
                byte c1 = value[i + 1];
                if (c0 == quote || c1 == quote || c0 == '\\' || c1 == '\\' || c0 < ' ' || c1 < ' ') {
                    special = true;
                } else {
                    i += 2;
                }
            }
            if (!special && i + 1 == value.length) {
                byte c0 = value[i];
                special = c0 == quote || c0 == '\\' || c0 < ' ';
            }
        }

        if (!special) {
            System.arraycopy(value, 0, bytes, off, value.length);
            off += value.length;
        } else {
            for (int i = 0; i < value.length; ++i) {
                byte ch = value[i];
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
                        } else if (coder == 0 && ch < 0) {
                            // latin
                            int c = ch & 0xFF;
                            bytes[off++] = (byte) (0xc0 | (c >> 6));
                            bytes[off++] = (byte) (0x80 | (c & 0x3f));
                        } else {
                            bytes[off++] = (byte) ch;
                        }
                        break;
                }
            }
        }
        bytes[off++] = (byte) quote;
    }
}
