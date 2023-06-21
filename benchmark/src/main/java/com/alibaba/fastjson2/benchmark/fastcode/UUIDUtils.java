package com.alibaba.fastjson2.benchmark.fastcode;

import com.alibaba.fastjson2.util.JDKUtils;

import java.util.UUID;

import static com.alibaba.fastjson2.util.JDKUtils.LATIN1;
import static com.alibaba.fastjson2.util.JDKUtils.UTF16;

public class UUIDUtils {
    static final char[] HEX256;

    static {
        HEX256 = new char[256];
        for (int i = 0; i < 256; i++) {
            int hi = (i >> 4) & 15;
            int lo = i & 15;
            HEX256[i] = (char) (((hi < 10 ? '0' + hi : 'a' + hi - 10) << 8)
                    + (lo < 10 ? '0' + lo : 'a' + lo - 10));
        }
    }

    public static String fastUUID(UUID uuid) {
        long hi = uuid.getMostSignificantBits();
        long lo = uuid.getLeastSignificantBits();

        final char[] hex256 = UUIDUtils.HEX256;
        char i = hex256[((int) (hi >> 56)) & 255];
        char i1 = hex256[((int) (hi >> 48)) & 255];
        char i2 = hex256[((int) (hi >> 40)) & 255];
        char i3 = hex256[((int) (hi >> 32)) & 255];
        char i4 = hex256[(((int) hi) >> 24) & 255];
        char i5 = hex256[(((int) hi) >> 16) & 255];
        char i6 = hex256[(((int) hi) >> 8) & 255];
        char i7 = hex256[((int) hi) & 255];
        char i8 = hex256[(((int) (lo >> 56))) & 255];
        char i9 = hex256[(((int) (lo >> 48))) & 255];
        char i10 = hex256[(((int) (lo >> 40))) & 255];
        char i11 = hex256[((int) (lo >> 32)) & 255];
        char i12 = hex256[(((int) lo) >> 24) & 255];
        char i13 = hex256[(((int) lo) >> 16) & 255];
        char i14 = hex256[(((int) lo) >> 8) & 255];
        char i15 = hex256[((int) lo) & 255];

        byte[] bytes = new byte[36];
        bytes[0] = (byte) (i >> 8);
        bytes[1] = (byte) i;
        bytes[2] = (byte) (i1 >> 8);
        bytes[3] = (byte) i1;
        bytes[4] = (byte) (i2 >> 8);
        bytes[5] = (byte) i2;
        bytes[6] = (byte) (i3 >> 8);
        bytes[7] = (byte) i3;
        bytes[8] = '-';
        bytes[9] = (byte) (i4 >> 8);
        bytes[10] = (byte) i4;
        bytes[11] = (byte) (i5 >> 8);
        bytes[12] = (byte) i5;
        bytes[13] = '-';
        bytes[14] = (byte) (i6 >> 8);
        bytes[15] = (byte) i6;
        bytes[16] = (byte) (i7 >> 8);
        bytes[17] = (byte) i7;
        bytes[18] = '-';
        bytes[19] = (byte) (i8 >> 8);
        bytes[20] = (byte) i8;
        bytes[21] = (byte) (i9 >> 8);
        bytes[22] = (byte) i9;
        bytes[23] = '-';
        bytes[24] = (byte) (i10 >> 8);
        bytes[25] = (byte) i10;
        bytes[26] = (byte) (i11 >> 8);
        bytes[27] = (byte) i11;
        bytes[28] = (byte) (i12 >> 8);
        bytes[29] = (byte) i12;
        bytes[30] = (byte) (i13 >> 8);
        bytes[31] = (byte) i13;
        bytes[32] = (byte) (i14 >> 8);
        bytes[33] = (byte) i14;
        bytes[34] = (byte) (i15 >> 8);
        bytes[35] = (byte) i15;
        return JDKUtils.STRING_CREATOR_JDK11.apply(bytes, LATIN1);
    }

    public static String fastUUID2(UUID uuid) {
        return fastUUID(uuid.getLeastSignificantBits(), uuid.getMostSignificantBits(), false);
    }

    static String fastUUID(long lsb, long msb) {
        char i = HEX256[((int) (msb >> 56)) & 255];
        char i1 = HEX256[((int) (msb >> 48)) & 255];
        char i2 = HEX256[((int) (msb >> 40)) & 255];
        char i3 = HEX256[((int) (msb >> 32)) & 255];
        char i4 = HEX256[(((int) msb) >> 24) & 255];
        char i5 = HEX256[(((int) msb) >> 16) & 255];
        char i6 = HEX256[(((int) msb) >> 8) & 255];
        char i7 = HEX256[((int) msb) & 255];
        char i8 = HEX256[(((int) (lsb >> 56))) & 255];
        char i9 = HEX256[(((int) (lsb >> 48))) & 255];
        char i10 = HEX256[(((int) (lsb >> 40))) & 255];
        char i11 = HEX256[((int) (lsb >> 32)) & 255];
        char i12 = HEX256[(((int) lsb) >> 24) & 255];
        char i13 = HEX256[(((int) lsb) >> 16) & 255];
        char i14 = HEX256[(((int) lsb) >> 8) & 255];
        char i15 = HEX256[((int) lsb) & 255];

        byte[] buf = new byte[36];
        buf[0] = (byte) (i >> 8);
        buf[1] = (byte) i;
        buf[2] = (byte) (i1 >> 8);
        buf[3] = (byte) i1;
        buf[4] = (byte) (i2 >> 8);
        buf[5] = (byte) i2;
        buf[6] = (byte) (i3 >> 8);
        buf[7] = (byte) i3;
        buf[8] = '-';
        buf[9] = (byte) (i4 >> 8);
        buf[10] = (byte) i4;
        buf[11] = (byte) (i5 >> 8);
        buf[12] = (byte) i5;
        buf[13] = '-';
        buf[14] = (byte) (i6 >> 8);
        buf[15] = (byte) i6;
        buf[16] = (byte) (i7 >> 8);
        buf[17] = (byte) i7;
        buf[18] = '-';
        buf[19] = (byte) (i8 >> 8);
        buf[20] = (byte) i8;
        buf[21] = (byte) (i9 >> 8);
        buf[22] = (byte) i9;
        buf[23] = '-';
        buf[24] = (byte) (i10 >> 8);
        buf[25] = (byte) i10;
        buf[26] = (byte) (i11 >> 8);
        buf[27] = (byte) i11;
        buf[28] = (byte) (i12 >> 8);
        buf[29] = (byte) i12;
        buf[30] = (byte) (i13 >> 8);
        buf[31] = (byte) i13;
        buf[32] = (byte) (i14 >> 8);
        buf[33] = (byte) i14;
        buf[34] = (byte) (i15 >> 8);
        buf[35] = (byte) i15;
        return new String(buf, LATIN1);
    }

    static String fastUUID(long lo, long hi, boolean COMPACT_STRINGS) {
        final char[] hex256 = UUIDUtils.HEX256;
        char i = hex256[((int) (hi >> 56)) & 255];
        char i1 = hex256[((int) (hi >> 48)) & 255];
        char i2 = hex256[((int) (hi >> 40)) & 255];
        char i3 = hex256[((int) (hi >> 32)) & 255];
        char i4 = hex256[(((int) hi) >> 24) & 255];
        char i5 = hex256[(((int) hi) >> 16) & 255];
        char i6 = hex256[(((int) hi) >> 8) & 255];
        char i7 = hex256[((int) hi) & 255];
        char i8 = hex256[(((int) (lo >> 56))) & 255];
        char i9 = hex256[(((int) (lo >> 48))) & 255];
        char i10 = hex256[(((int) (lo >> 40))) & 255];
        char i11 = hex256[((int) (lo >> 32)) & 255];
        char i12 = hex256[(((int) lo) >> 24) & 255];
        char i13 = hex256[(((int) lo) >> 16) & 255];
        char i14 = hex256[(((int) lo) >> 8) & 255];
        char i15 = hex256[((int) lo) & 255];

        if (COMPACT_STRINGS) {
            byte[] bytes = new byte[36];
            bytes[0] = (byte) (i >> 8);
            bytes[1] = (byte) i;
            bytes[2] = (byte) (i1 >> 8);
            bytes[3] = (byte) i1;
            bytes[4] = (byte) (i2 >> 8);
            bytes[5] = (byte) i2;
            bytes[6] = (byte) (i3 >> 8);
            bytes[7] = (byte) i3;
            bytes[8] = '-';
            bytes[9] = (byte) (i4 >> 8);
            bytes[10] = (byte) i4;
            bytes[11] = (byte) (i5 >> 8);
            bytes[12] = (byte) i5;
            bytes[13] = '-';
            bytes[14] = (byte) (i6 >> 8);
            bytes[15] = (byte) i6;
            bytes[16] = (byte) (i7 >> 8);
            bytes[17] = (byte) i7;
            bytes[18] = '-';
            bytes[19] = (byte) (i8 >> 8);
            bytes[20] = (byte) i8;
            bytes[21] = (byte) (i9 >> 8);
            bytes[22] = (byte) i9;
            bytes[23] = '-';
            bytes[24] = (byte) (i10 >> 8);
            bytes[25] = (byte) i10;
            bytes[26] = (byte) (i11 >> 8);
            bytes[27] = (byte) i11;
            bytes[28] = (byte) (i12 >> 8);
            bytes[29] = (byte) i12;
            bytes[30] = (byte) (i13 >> 8);
            bytes[31] = (byte) i13;
            bytes[32] = (byte) (i14 >> 8);
            bytes[33] = (byte) i14;
            bytes[34] = (byte) (i15 >> 8);
            bytes[35] = (byte) i15;
            return JDKUtils.STRING_CREATOR_JDK11.apply(bytes, LATIN1);
        } else {
            byte[] buf = new byte[72];
            putChar(buf, 0, (byte) (i >> 8));
            putChar(buf, 1, (byte) i);
            putChar(buf, 2, (byte) (i1 >> 8));
            putChar(buf, 3, (byte) i1);
            putChar(buf, 4, (byte) (i2 >> 8));
            putChar(buf, 5, (byte) i2);
            putChar(buf, 6, (byte) (i3 >> 8));
            putChar(buf, 7, (byte) i3);
            putChar(buf, 8, '-');
            putChar(buf, 9, (byte) (i4 >> 8));
            putChar(buf, 10, (byte) i4);
            putChar(buf, 11, (byte) (i5 >> 8));
            putChar(buf, 12, (byte) i5);
            putChar(buf, 13, '-');
            putChar(buf, 14, (byte) (i6 >> 8));
            putChar(buf, 15, (byte) i6);
            putChar(buf, 16, (byte) (i7 >> 8));
            putChar(buf, 17, (byte) i7);
            putChar(buf, 18, '-');
            putChar(buf, 19, (byte) (i8 >> 8));
            putChar(buf, 20, (byte) i8);
            putChar(buf, 21, (byte) (i9 >> 8));
            putChar(buf, 22, (byte) i9);
            putChar(buf, 23, '-');
            putChar(buf, 24, (byte) (i10 >> 8));
            putChar(buf, 25, (byte) i10);
            putChar(buf, 26, (byte) (i11 >> 8));
            putChar(buf, 27, (byte) i11);
            putChar(buf, 28, (byte) (i12 >> 8));
            putChar(buf, 29, (byte) i12);
            putChar(buf, 30, (byte) (i13 >> 8));
            putChar(buf, 31, (byte) i13);
            putChar(buf, 32, (byte) (i14 >> 8));
            putChar(buf, 33, (byte) i14);
            putChar(buf, 34, (byte) (i15 >> 8));
            putChar(buf, 35, (byte) i15);
            return JDKUtils.STRING_CREATOR_JDK11.apply(buf, UTF16);
        }
    }

    static final int HI_BYTE_SHIFT = 0;
    static final int LO_BYTE_SHIFT = 8;

    static void putChar(byte[] val, int index, int c) {
        index <<= 1;
        val[index++] = (byte) (c >> HI_BYTE_SHIFT);
        val[index] = (byte) (c >> LO_BYTE_SHIFT);
    }
}
