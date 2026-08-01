package org.apache.dubbo.jsonb.rw;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class BigDecimalUtil {
    public static final DecimalFormat DF_TWO = new DecimalFormat("###,##0.00");
    public static final DecimalFormat DF_FOUR = new DecimalFormat("###,##0.0000");
    public static final DecimalFormat DF_SIX = new DecimalFormat("###,##0.000000");
    public static final DecimalFormat EIGHT_SIX = new DecimalFormat("###,##0.00000000");

    public static String formatDecimal(BigDecimal value) {
        if (null == value) {
            return null;
        }

        int scale = value.scale();
        DecimalFormat decimalFormat;
        if (scale <= 2) {
            decimalFormat = DF_TWO;
        } else if (scale <= 4) {
            // 保留四位小数
            decimalFormat = DF_FOUR;
        } else if (scale <= 6) {
            // 保留六位小数
            decimalFormat = DF_SIX;
        } else if (scale <= 8) {
            decimalFormat = EIGHT_SIX;
        } else {
            decimalFormat = null;
        }

        if (null == decimalFormat) {
            return value.toString();
        }
        // FIXME: decimalFormat is NOT thread safe
        return decimalFormat.format(value);
    }

    public static BigDecimal castToBigDecimal(String value) {
        final int len;
        if (value == null || (len = value.length()) == 0) {
            return null;
        }

        // 只保留数字和小数点
        final char[] validChars = new char[len];
        int i = 0;
        for (int j = 0; j < len; j++) {
            final char c = value.charAt(j);
            if (c >= '0' && c <= '9' || c == '.') {
                validChars[i++] = c;
            }
        }

        if (i == 0) {
            return null;
        }

        // new BigDecimal( stringBuilder.toString() ) will call new BigDecimal( str.toCharArray() ) internally
        return new BigDecimal(validChars, 0, i);
    }
}
