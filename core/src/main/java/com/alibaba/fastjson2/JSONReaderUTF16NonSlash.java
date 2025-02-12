package com.alibaba.fastjson2;

import static com.alibaba.fastjson2.util.JDKUtils.ANDROID;
import static com.alibaba.fastjson2.util.JDKUtils.JVM_VERSION;

final class JSONReaderUTF16NonSlash
        extends JSONReaderUTF16 {
    JSONReaderUTF16NonSlash(Context ctx, String str, char[] chars, int offset, int length) {
        super(ctx, str, chars, offset, length);
    }

    @Override
    public String readString() {
        final char[] chars = this.chars;
        if (ch == '"' || ch == '\'') {
            final char quote = ch;
            int offset = this.offset;
            final int start = offset, end = this.end;

            for (; ; offset++) {
                if (offset >= end) {
                    throw new JSONException(info("invalid escape character EOI"));
                }
                if (chars[offset] == quote) {
                    break;
                }
            }

            String str;
            if (this.str != null && (JVM_VERSION > 8 || ANDROID)) {
                str = this.str.substring(start, offset);
            } else {
                str = new String(chars, start, offset - start);
            }

            if ((context.features & Feature.TrimString.mask) != 0) {
                str = str.trim();
            }
            // empty string to null
            if (str.isEmpty() && (context.features & Feature.EmptyStringAsNull.mask) != 0) {
                str = null;
            }

            int ch = ++offset == end ? EOI : chars[offset++];
            while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }

            if (comma = ch == ',') {
                ch = offset == end ? EOI : chars[offset++];
                while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                    ch = offset == end ? EOI : chars[offset++];
                }
            }

            this.ch = (char) ch;
            this.offset = offset;
            return str;
        }

        return readStringNotMatch();
    }
}
