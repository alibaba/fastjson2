package com.alibaba.fastjson2;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.alibaba.fastjson2.util.JDKUtils.*;

final class JSONReaderASCIINonSlash
        extends JSONReaderASCII{
    JSONReaderASCIINonSlash(Context ctx, String str, byte[] bytes, int offset, int length) {
        super(ctx, str, bytes, offset, length);
    }

    @Override
    public final String readString() {
        int ch = this.ch;
        if (ch == '"' || ch == '\'') {
            final byte[] bytes = this.bytes;

            int offset = this.offset;
            final int start = offset, end = this.end;

            int index;
            try {
                index = (int) INDEX_OF_CHAR_LATIN1.invokeExact(bytes, ch, offset, end);
            } catch (Throwable e) {
                throw new JSONException(e.getMessage());
            }
            if (index == -1) {
                throw error("invalid escape character EOI");
            }
            offset = index + 1;

            String str = STRING_CREATOR_JDK11 != null
                    ? STRING_CREATOR_JDK11.apply(Arrays.copyOfRange(bytes, start, index), LATIN1)
                    : new String(bytes, start, index - start, StandardCharsets.ISO_8859_1);
            long features = context.features;
            if ((features & MASK_TRIM_STRING) != 0) {
                str = str.trim();
            }
            str = (features & MASK_EMPTY_STRING_AS_NULL) != 0 && str.isEmpty() ? null : str;

            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if (comma = ch == ',') {
                ch = offset == end ? EOI : bytes[offset++];
                while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                    ch = offset == end ? EOI : bytes[offset++];
                }
            }

            this.ch = (char) (ch & 0xFF);
            this.offset = offset;
            return str;
        }

        return readStringNotMatch();
    }
}
