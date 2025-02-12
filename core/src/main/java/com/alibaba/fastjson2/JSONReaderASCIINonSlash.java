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
                throw new JSONException("invalid escape character EOI");
            }

            String str;
            if (STRING_CREATOR_JDK11 != null) {
                str = STRING_CREATOR_JDK11.apply(Arrays.copyOfRange(this.bytes, this.offset, offset), LATIN1);
            } else {
                str = new String(bytes, start, offset - start, StandardCharsets.ISO_8859_1);
            }

            long features = context.features;
            if ((features & Feature.TrimString.mask) != 0) {
                str = str.trim();
            }
            // empty string to null
            if (str.isEmpty() && (features & Feature.EmptyStringAsNull.mask) != 0) {
                str = null;
            }

            valueEnd(bytes, offset, end);
            return str;
        }

        return readStringNotMatch();
    }
}
