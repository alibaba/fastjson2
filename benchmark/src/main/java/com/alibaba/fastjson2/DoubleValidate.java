package com.alibaba.fastjson2;

import com.alibaba.fastjson2.misc.FloatingDecimal;

import java.text.NumberFormat;

public class DoubleValidate {
    static final NumberFormat format = NumberFormat.getNumberInstance();
    static final char[] chars = new char[]{
            '0', '.',
            '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0'
    };

    public static void main(String[] args) throws Exception {
        long startMillis = System.currentTimeMillis();

        final int E0 = 10, E1 = 10, E2 = 10, E3 = 10, E4 = 10, E5 = 10, E6 = 10, E7 = 10, E8 = 10, E9 = 10;
        final int E10 = 0, E11 = 0, E12 = 0, E13 = 0, E14 = 0, E15 = 0;

        for (int i0 = 0; i0 < E0; i0++) {
            char c0 = (char) ('0' + i0);
            chars[2] = c0;

            for (int i1 = 0; i1 < E1; i1++) {
                long x1 = i0 * 10 + i1;
                char c1 = (char) ('0' + i1);
                chars[3] = c1;

                for (int i2 = 0; i2 < E2; i2++) {
                    long x2 = x1 * 10 + i2;
                    char c2 = (char) ('0' + i2);
                    chars[4] = c2;

                    for (int i3 = 0; i3 < E3; i3++) {
                        long x3 = x2 * 10 + i3;
                        char c3 = (char) ('0' + i3);
                        chars[5] = c3;

                        for (int i4 = 0; i4 < E4; i4++) {
                            long x4 = x3 * 10 + i4;
                            char c4 = (char) ('0' + i4);
                            chars[6] = c4;

                            for (int i5 = 0; i5 < E5; i5++) {
                                long x5 = x4 * 10 + i5;
                                char c5 = (char) ('0' + i5);
                                chars[7] = c5;

                                for (int i6 = 0; i6 < E6; i6++) {
                                    long x6 = x5 * 10 + i6;
                                    char c6 = (char) ('0' + i6);
                                    chars[8] = c6;

                                    double d6 = x6 / JSONFactory.SMALL_10_POW[7];
                                    if (d6 == 0) {
                                        continue;
                                    }
                                    double dx6 = FloatingDecimal.parseDouble(chars, 9);
                                    if (dx6 != d6) {
                                        throw new JSONException("not match " + dx6);
                                    }

                                    for (int i7 = 0; i7 < E7; i7++) {
                                        long x7 = x6 * 10 + i7;
                                        char c7 = (char) ('0' + i7);
                                        chars[9] = c7;

                                        double d7 = x7 / JSONFactory.SMALL_10_POW[8];
                                        if (d7 == 0) {
                                            continue;
                                        }
                                        double dx7 = FloatingDecimal.parseDouble(chars, 10);
                                        if (dx7 != d7) {
                                            throw new JSONException("not match " + dx7);
                                        }

                                        for (int i8 = 0; i8 < E8; i8++) {
                                            long x8 = x7 * 10 + i8;
                                            char c8 = (char) ('0' + i8);
                                            chars[10] = c8;

                                            double d8 = x8 / JSONFactory.SMALL_10_POW[9];
                                            if (d8 == 0) {
                                                continue;
                                            }
                                            double dx8 = FloatingDecimal.parseDouble(chars, 11);
                                            if (dx8 != d8) {
                                                throw new JSONException("not match " + dx8);
                                            }

                                            for (int i9 = 0; i9 < E9; i9++) {
                                                long x9 = x8 * 10 + i9;
                                                char c9 = (char) ('0' + i9);
                                                chars[11] = c9;

                                                double d9 = x9 / JSONFactory.SMALL_10_POW[10];
                                                if (d9 == 0) {
                                                    continue;
                                                }
                                                double dx9 = FloatingDecimal.parseDouble(chars, 12);
                                                if (dx9 != d9) {
                                                    throw new JSONException("not match " + dx9);
                                                }

//                                                for (int i10 = 0; i10 < E10; i10++) {
//                                                    long x10 = x9 * 10 + i10;
//                                                    char c10 = (char) ('0' + i10);
//                                                    chars[12] = c10;
//
//                                                    double d10 = x10 / JSONFactory.SMALL_10_POW[11];
//                                                    if (d10 == 0) {
//                                                        continue;
//                                                    }
//                                                    double dx10 = FloatingDecimal.parseDouble(chars, 13);
//                                                    if (dx10 != d10) {
//                                                        throw new JSONException("not match " + dx10);
//                                                    }
//
//                                                    for (int i11 = 0; i11 < E11; i11++) {
//                                                        long x11 = x10 * 10 + i11;
//                                                        char c11 = (char) ('0' + i11);
//                                                        chars[13] = c11;
//
//                                                        double d11 = x11 / JSONFactory.SMALL_10_POW[12];
//                                                        if (d11 == 0) {
//                                                            continue;
//                                                        }
//                                                        double dx11 = FloatingDecimal.parseDouble(chars, 14);
//                                                        if (dx11 != d11) {
//                                                            throw new JSONException("not match " + dx11);
//                                                        }
//
//                                                        for (int i12 = 0; i12 < E12; i12++) {
//                                                            long x12 = x11 * 10 + i12;
//                                                            char c12 = (char) ('0' + i12);
//                                                            chars[14] = c12;
//
//                                                            double d12 = x12 / JSONFactory.SMALL_10_POW[13];
//                                                            if (d12 == 0) {
//                                                                continue;
//                                                            }
//                                                            double dx12 = FloatingDecimal.parseDouble(chars, 15);
//                                                            if (dx12 != d12) {
//                                                                throw new JSONException("not match " + dx12);
//                                                            }
//
//                                                            for (int i13 = 0; i13 < E13; i13++) {
//                                                                long x13 = x12 * 10 + i13;
//                                                                char c13 = (char) ('0' + i13);
//                                                                chars[16] = c13;
//
//                                                                double d13 = x13 / JSONFactory.SMALL_10_POW[14];
//                                                                if (d13 == 0) {
//                                                                    continue;
//                                                                }
//                                                                double dx13 = FloatingDecimal.parseDouble(chars, 16);
//                                                                if (dx13 != d13) {
//                                                                    throw new JSONException("not match " + dx13);
//                                                                }
//
//                                                                for (int i14 = 0; i14 < E14; i14++) {
//                                                                    long x14 = x13 * 10 + i14;
//                                                                    char c14 = (char) ('0' + i14);
//                                                                    chars[16] = c14;
//
//                                                                    double d14 = x14 / JSONFactory.SMALL_10_POW[15];
//                                                                    if (d14 == 0) {
//                                                                        continue;
//                                                                    }
//                                                                    double dx14 = FloatingDecimal.parseDouble(chars, 17);
//                                                                    if (dx14 != d14) {
//                                                                        throw new JSONException("not match " + dx14);
//                                                                    }
//
//                                                                    for (int i15 = 0; i15 < E15; i15++) {
//                                                                        long x15 = x14 * 10 + i15;
//                                                                        char c15 = (char) ('0' + i15);
//                                                                        chars[15] = c15;
//
//                                                                        double d15 = x15 / JSONFactory.SMALL_10_POW[16];
//                                                                        if (d15 == 0) {
//                                                                            continue;
//                                                                        }
//                                                                        double dx15 = FloatingDecimal.parseDouble(chars, 18);
//                                                                        if (dx15 != d15) {
//                                                                            throw new JSONException("not match " + dx15);
//                                                                        }
//                                                                    }
//                                                                }
//                                                            }
//                                                        }
//                                                    }
//                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        long millis = System.currentTimeMillis() - startMillis;
        System.out.println("completed, millis " + format.format(millis));
    }
}
