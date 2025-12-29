package com.alibaba.fastjson2.internal;

public class ByteArray {
    public int digit1(byte[] buf, int off) {
        int d = buf[off] - '0';
        return d >= 0 && d <= 9 ? d : -1;
    }

    public int digit1(char[] buf, int off) {
        int d = buf[off] - '0';
        return d >= 0 && d <= 9 ? d : -1;
    }

    public int digit2(byte[] str, int offset) {
        byte c0 = str[offset], c1 = str[offset + 1];
        if (c0 >= '0' && c0 <= '9' && c1 >= '0' && c1 <= '9') {
            return c0 * 10 + c1 - ('0' * 10 + '0');
        }
        return -1;
    }

    public int digit2(char[] str, int offset) {
        char c0 = str[offset], c1 = str[offset + 1];
        if (c0 >= '0' && c0 <= '9' && c1 >= '0' && c1 <= '9') {
            return c0 * 10 + c1 - ('0' * 10 + '0');
        }
        return -1;
    }

    public int digit3(byte[] str, int offset) {
        byte c0 = str[offset], c1 = str[offset + 1], c2 = str[offset + 2];
        if (c0 >= '0' && c0 <= '9' && c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9') {
            return c0 * 100 + c1 * 10 + c2 - ('0' * 100 + '0' * 10 + '0');
        }
        return -1;
    }

    public int digit3(char[] str, int offset) {
        char c0 = str[offset], c1 = str[offset + 1], c2 = str[offset + 2];
        if (c0 >= '0' && c0 <= '9' && c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9') {
            return c0 * 100 + c1 * 10 + c2 - ('0' * 100 + '0' * 10 + '0');
        }
        return -1;
    }

    public int digit4(byte[] str, int offset) {
        byte c0 = str[offset], c1 = str[offset + 1], c2 = str[offset + 2], c3 = str[offset + 3];
        if (c0 >= '0' && c0 <= '9' && c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9' && c3 >= '0' && c3 <= '9') {
            return c0 * 1000 + c1 * 100 + c2 * 10 + c3 - ('0' * 1000 + '0' * 100 + '0' * 10 + '0');
        }
        return -1;
    }

    public int digit4(char[] str, int offset) {
        char c0 = str[offset], c1 = str[offset + 1], c2 = str[offset + 2], c3 = str[offset + 3];
        if (c0 >= '0' && c0 <= '9' && c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9' && c3 >= '0' && c3 <= '9') {
            return c0 * 1000 + c1 * 100 + c2 * 10 + c3 - ('0' * 1000 + '0' * 100 + '0' * 10 + '0');
        }
        return -1;
    }
}
