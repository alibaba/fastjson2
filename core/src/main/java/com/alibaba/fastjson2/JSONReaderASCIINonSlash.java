package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.alibaba.fastjson2.util.JDKUtils.LATIN1;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_CREATOR_JDK11;

final class JSONReaderASCIINonSlash
        extends JSONReaderASCII {
    JSONReaderASCIINonSlash(Context ctx, String str, byte[] bytes, int offset, int length) {
        super(ctx, str, bytes, offset, length);
    }

    @Override
    public String readString() {
        char quote = this.ch;
        if (quote == '"' || quote == '\'') {
            final byte[] bytes = this.bytes;
            final int start = offset, end = this.end;
            int offset = IOUtils.indexOfQuote(bytes, quote, start, end);
            if (offset == -1) {
                throw error("invalid escape character EOI");
            }

            String str;
            if (STRING_CREATOR_JDK11 != null) {
                str = STRING_CREATOR_JDK11.apply(Arrays.copyOfRange(bytes, start, offset), LATIN1);
            } else {
                str = new String(bytes, start, offset - start, StandardCharsets.ISO_8859_1);
            }
            long features = context.features;
            if ((features & (MASK_TRIM_STRING | MASK_EMPTY_STRING_AS_NULL)) != 0) {
                str = stringValue(str, features);
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
