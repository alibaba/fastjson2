package com.alibaba.fastjson2;

import java.util.Arrays;

final class JSONWriterUTF16CSV
        extends JSONWriterUTF16 {
    final char columnSeparator;
    final char rowSeparator;

    JSONWriterUTF16CSV(Context ctx) {
        this(ctx, ',', '\n');
    }

    JSONWriterUTF16CSV(Context ctx, char columnSeparator, char rowSeparator) {
        super(ctx);
        this.columnSeparator = columnSeparator;
        this.rowSeparator = rowSeparator;
    }

    public boolean isCSV() {
        return true;
    }

    @Override
    public void startArray() {
    }

    @Override
    public void endArray() {
        writeRaw('\n');
    }

    @Override
    public void writeNull() {
    }

    @Override
    public void writeString(String str) {
        if (str == null) {
            writeNull();
            return;
        }

        final int len = str.length();
        boolean escape = false;
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (ch == columnSeparator || ch == rowSeparator) {
                escape = true;
                break;
            }
        }

        if (!escape) {
            int minCapacity = off + len;
            if (minCapacity - chars.length > 0) {
                int oldCapacity = chars.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - maxArraySize > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                chars = Arrays.copyOf(chars, newCapacity);
            }
            str.getChars(0, len, chars, off);
            off += len;
            return;
        }

        throw new JSONException("TODO");
    }
}
