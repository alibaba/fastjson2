package com.alibaba.fastjson2.util;

/**
 * Date 2024/5/25 10:46
 * Created by wangyc
 */
public class Scientific {
    public final long output;
    public final int count;
    public final int e10;
    public final boolean b;

    public static final Scientific SCIENTIFIC_NULL = new Scientific(0, true);
    public static final Scientific ZERO = new Scientific(0, 3, 0);
    public static final Scientific NEGATIVE_ZERO = new Scientific(0, 3, 0);
    public static final Scientific DOUBLE_MIN = new Scientific(49, 2, -324);  // 4.9E-324

    public Scientific(long output, int count, int e10) {
        this.output = output;
        this.count = count;
        this.e10 = e10;
        this.b = false;
    }

    public Scientific(int e10, boolean b) {
        this.e10 = e10;
        this.b = b;
        this.output = 0;
        this.count = 0;
    }

    @Override
    public String toString() {
        if (this == SCIENTIFIC_NULL) {
            return "null";
        }
        if (this == ZERO) {
            return "0.0";
        }
        if (this == NEGATIVE_ZERO) {
            return "-0.0";
        }
        if (b) {
            return "1e" + e10;
        }
        return output + "|" + e10;
    }
}
