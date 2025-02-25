package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.INDEX_OF_CHAR_LATIN1;

final class JSONReaderASCIISlash
        extends JSONReaderASCII {
    static final int ESCAPE_INDEX_NOT_SET = -2;
    private int nextEscapeIndex = ESCAPE_INDEX_NOT_SET;

    JSONReaderASCIISlash(Context ctx, String str, byte[] bytes, int offset, int length) {
        this(ctx, str, bytes, offset, length, ESCAPE_INDEX_NOT_SET);
    }

    JSONReaderASCIISlash(Context ctx, String str, byte[] bytes, int offset, int length, int nextEscapeIndex) {
        super(ctx, str, bytes, offset, length);
        this.nextEscapeIndex = nextEscapeIndex;
    }

    JSONReaderASCIISlash(Context ctx, InputStream is) {
        super(ctx, is);
    }

    @Override
    public final String readString() {
        int ch = this.ch;
        if (ch == '"' || ch == '\'') {
            final byte[] bytes = this.bytes;

            int offset = this.offset;
            final int start = offset, end = this.end;

            int index;
            if (INDEX_OF_CHAR_LATIN1 == null) {
                index = IOUtils.indexOfQuoteV(bytes, ch, offset, end);
            } else {
                try {
                    index = (int) INDEX_OF_CHAR_LATIN1.invokeExact(bytes, ch, offset, end);
                }
                catch (Throwable e) {
                    throw new JSONException(e.getMessage());
                }
            }
            if (index == -1) {
                throw error("invalid escape character EOI");
            }
            int slashIndex = indexOfSlash(bytes, offset, end);
            if (slashIndex == -1 || slashIndex > index) {
                offset = index + 1;
            } else {
                return readEscaped(bytes, slashIndex, start, end, slashIndex - offset, ch);
            }

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

    private int indexOfSlash(byte[] bytes, int offset, int end) {
        int slashIndex = nextEscapeIndex;
        if (slashIndex == ESCAPE_INDEX_NOT_SET || (slashIndex != -1 && slashIndex < offset)) {
            nextEscapeIndex = slashIndex = IOUtils.indexOfSlash(bytes, offset, end);
        }
        return slashIndex;
    }
}
