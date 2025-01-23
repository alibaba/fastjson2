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
        DecimalFormat decimalFormat = null;
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
        } else {
            return decimalFormat.format(value);
        }
    }

    public static BigDecimal castToBigDecimal(String value) {
        if (null == value || value.isEmpty()) {
            return null;
        }

        // 只保留数字和小数点
        StringBuilder sb = new StringBuilder();
        char[] charArr = value.toCharArray();
        for (char c : charArr) {
            // 0-9
            if (c >= 48 && c <= 57) {
                sb.append(c);
            }

            // .
            if (c == 46) {
                sb.append(c);
            }
        }

        if (sb.length() < 1) {
            return null;
        }

        String decimal = sb.toString();
        return new BigDecimal(decimal);
    }
}
