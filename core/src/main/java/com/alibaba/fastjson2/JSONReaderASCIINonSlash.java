package com.alibaba.fastjson2;

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
            int offset = this.offset;
            final int start = offset, end = this.end;
            valueEscape = false;
            for (; ; offset++) {
                if (offset >= end) {
                    throw new JSONException("invalid escape character EOI");
                }
                if (bytes[offset] == quote) {
                    break;
                }
            }

            String str = subString(bytes, start, offset);

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
