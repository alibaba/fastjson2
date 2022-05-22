package com.alibaba.fastjson2.support.odps;

import com.aliyun.odps.io.Writable;
import com.aliyun.odps.io.WritableUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JSONWritable
        implements Writable {
    private static final byte[] EMPTY_BYTES = new byte[0];

    byte[] bytes;
    int off;
    int length;

    public JSONWritable() {
        bytes = EMPTY_BYTES;
    }

    public JSONWritable(byte[] bytes) {
        this.bytes = bytes;
        this.off = 0;
        this.length = bytes.length;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        WritableUtils.writeVInt(out, length);
        out.write(bytes, off, length);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        int newLength = WritableUtils.readVInt(in);
        setCapacity(newLength, false);
        in.readFully(bytes, 0, newLength);
        length = newLength;
    }

    void setCapacity(int len, boolean keepData) {
        if (bytes == null || bytes.length < len) {
            byte[] newBytes = new byte[len];
            if (bytes != null && keepData) {
                System.arraycopy(bytes, 0, newBytes, 0, length);
            }
            bytes = newBytes;
        }
    }

    public void set(String string) {
        this.bytes = string.getBytes(StandardCharsets.UTF_8);
        this.length = bytes.length;
    }

    public void set(byte[] utf8) {
        set(utf8, 0, utf8.length);
    }

    public void set(byte[] utf8, int start, int len) {
        setCapacity(len, false);
        System.arraycopy(utf8, start, bytes, 0, len);
        this.length = len;
    }

    @Override
    public String toString() {
        return new String(bytes, off, length, StandardCharsets.UTF_8);
    }
}
