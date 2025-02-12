package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;

import java.nio.charset.StandardCharsets;

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
                throw new JSONException("invalid escape character EOI");
            }

            String str = new String(bytes, start, offset - start, StandardCharsets.ISO_8859_1);
            long features = context.features;
            if ((features & Feature.TrimString.mask) != 0) {
                str = str.trim();
            }
            // empty string to null
            if (str.isEmpty() && (features & Feature.EmptyStringAsNull.mask) != 0) {
                str = null;
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
