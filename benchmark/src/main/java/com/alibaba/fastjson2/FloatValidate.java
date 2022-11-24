package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.text.NumberFormat;
import java.util.Arrays;

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

        chars[0] = '0';
        chars[1] = '.';

        long max = 1;
        for (int i = 0; i < 10; i++) {
            max *= 10;

            int index = 3 + i;
            int p = i + 1;
            Arrays.fill(chars, 2, chars.length, '0');

            long startMillis = System.currentTimeMillis();
            for (long j = 0; j < max; ++j) {
                IOUtils.getChars(j, index, chars);

                float f0 = TypeUtils.parseFloat(chars, 0, index);
                float f1 = (float) (j / JSONFactory.SMALL_10_POW[p]);
                if (f0 != f1) {
                    String str = new String(chars, 0, index);
                    throw new JSONException("not match : " + str);
                }
            }

            long millis = System.currentTimeMillis() - startMillis;
            System.out.println(p + "-completed, millis " + format.format(millis));
        }
    }
}
