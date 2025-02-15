package com.alibaba.fastjson2.util;

/**
 * Date 2024/5/25 10:46
 * Created by wangyc
 */
public class Scientific {
    public long output;
    public int count;
    public final int e10;
    public final boolean b;

    public static final Scientific SCIENTIFIC_NULL = new Scientific(0, true);

    public Scientific(long output, int count, int e10) {
        this.output = output;
        this.count = count;
        this.e10 = e10;
        this.b = false;
    }

    public Scientific(int e10, boolean b) {
        this.e10 = e10;
        this.b = b;
    }

    @Override
    public String toString() {
        if (this == SCIENTIFIC_NULL) {
            return "null";
        }
        if (b) {
            return "1e" + e10;
        }
        return output + "|" + e10;
    }
}
