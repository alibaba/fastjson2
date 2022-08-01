package com.alibaba.fastjson2;

import com.alibaba.fastjson2.misc.FloatingDecimal;

import java.text.NumberFormat;

public class FloatValidate {
    static final NumberFormat format = NumberFormat.getNumberInstance();

    public static void main(String[] args) throws Exception {
        char[] chars = new char[]{
                '0', '.',
                '0', '0', '0', '0', '0',
                '0', '0', '0', '0', '0',
                '0', '0', '0', '0', '0',
                '0', '0', '0', '0', '0'
        };

        long startMillis = System.currentTimeMillis();

        for (int i0 = 0; i0 < 2; i0++) {
            char c0 = (char) ('0' + i0);
            chars[2] = c0;

            for (int i1 = 0; i1 < 10; i1++) {
                long x1 = i0 * 10 + i1;
                char c1 = (char) ('0' + i1);
                chars[3] = c1;

                for (int i2 = 0; i2 < 10; i2++) {
                    long x2 = x1 * 10 + i2;
                    char c2 = (char) ('0' + i2);
                    chars[4] = c2;

                    for (int i3 = 0; i3 < 10; i3++) {
                        long x3 = x2 * 10 + i3;
                        char c3 = (char) ('0' + i3);
                        chars[5] = c3;

                        for (int i4 = 0; i4 < 10; i4++) {
                            long x4 = x3 * 10 + i4;
                            char c4 = (char) ('0' + i4);
                            chars[6] = c4;

                            for (int i5 = 0; i5 < 10; i5++) {
                                long x5 = x4 * 10 + i5;
                                char c5 = (char) ('0' + i5);
                                chars[7] = c5;

                                for (int i6 = 0; i6 < 10; i6++) {
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

                                    for (int i7 = 0; i7 < 10; i7++) {
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

                                        for (int i8 = 0; i8 < 10; i8++) {
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

                                            for (int i9 = 0; i9 < 10; i9++) {
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
//
//                                                for (int i10 = 0; i10 < 10; i10++) {
//                                                    char c10 = (char) ('0' + i10);
//                                                    chars[12] = c10;
//
//                                                    for (int i11 = 0; i11 < 10; i11++) {
//                                                        char c11 = (char) ('0' + i11);
//                                                        chars[13] = c11;
//
//                                                        for (int i12 = 0; i12 < 10; i12++) {
//                                                            char c12 = (char) ('0' + i12);
//                                                            chars[14] = c12;
//
//                                                            for (int i13 = 0; i13 < 10; i13++) {
//                                                                char c13 = (char) ('0' + i13);
//                                                                chars[16] = c13;
//
//                                                                for (int i14 = 0; i14 < 10; i14++) {
//                                                                    char c14 = (char) ('0' + i14);
//                                                                    chars[16] = c14;
//
//                                                                    for (int i15 = 0; i15 < 10; i15++) {
//                                                                        char c15 = (char) ('0' + i15);
//                                                                        chars[15] = c15;
//
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
