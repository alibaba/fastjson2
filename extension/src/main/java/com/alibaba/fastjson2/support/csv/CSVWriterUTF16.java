package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.JDKUtils;

import java.io.IOException;
import java.io.Writer;

final class CSVWriterUTF16
        extends CSVWriter {
    final Writer writer;

    public CSVWriterUTF16(Writer writer) {
        this.writer = writer;
    }

    public void writeRow(Object... values) {
        throw new JSONException("unsupported operation");
    }

    public void writeString(String str) {
        if (str == null) {
            return;
        }

        char[] chars = JDKUtils.getCharArray(str);
        writeString(chars);
    }

    @Override
    public void writeRaw(String str) {
        throw new JSONException("unsupported operation");
    }

    public void writeString(char[] str) {
        throw new JSONException("unsupported operation");
    }

    protected void writeRaw(char ch) {
        throw new JSONException("unsupported operation");
    }

    @Override
    public void close() throws IOException {
        throw new JSONException("unsupported operation");
    }

    @Override
    public void flush() throws IOException {
        throw new JSONException("unsupported operation");
    }
}
