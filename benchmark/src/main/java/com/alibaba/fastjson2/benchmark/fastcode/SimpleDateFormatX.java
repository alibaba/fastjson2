package com.alibaba.fastjson2.benchmark.fastcode;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SimpleDateFormatX
        extends SimpleDateFormat {
    transient boolean format19;
    transient boolean format10;

    public SimpleDateFormatX(String pattern) {
        super(pattern);
        switch (pattern) {
            case "yyyy-MM-dd HH:mm:ss":
                format19 = true;
                break;
            case "yyyy-MM-dd":
                format10 = true;
                break;
            default:
                break;
        }
    }

    @Override
    public StringBuffer format(
            Date date,
            StringBuffer toAppendTo,
            FieldPosition pos
    ) {
        if (format19 || format10) {
            calendar.setTime(date);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            toAppendTo.append(year).append('-');
            if (month < 10) {
                toAppendTo.append('0');
            }
            toAppendTo.append(month).append('-');
            if (dayOfMonth < 10) {
                toAppendTo.append('0');
            }
            toAppendTo.append(dayOfMonth);
            if (format10) {
                return toAppendTo;
            }

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);

            toAppendTo.append(' ');
            if (hour < 10) {
                toAppendTo.append('0');
            }
            toAppendTo.append(hour).append(':');
            if (minute < 10) {
                toAppendTo.append('0');
            }
            toAppendTo.append(minute).append(':');
            if (second < 10) {
                toAppendTo.append('0');
            }
            toAppendTo.append(second);
            return toAppendTo;
        }

        return super.format(date, toAppendTo, pos);
    }

    public Date parse(String text) throws ParseException {
        if (text != null) {
            final int textLength = text.length();
            if ((format10 && textLength == 10) || (format19 && textLength == 19)) {
                char c0 = text.charAt(0);
                char c1 = text.charAt(1);
                char c2 = text.charAt(2);
                char c3 = text.charAt(3);
                char c4 = text.charAt(4);
                char c5 = text.charAt(5);
                char c6 = text.charAt(6);
                char c7 = text.charAt(7);
                char c8 = text.charAt(8);
                char c9 = text.charAt(9);

                int year, month, dayOfMonth;
                if (c0 >= '0' && c0 <= '9'
                        && c1 >= '0' && c1 <= '9'
                        && c2 >= '0' && c2 <= '9'
                        && c3 >= '0' && c3 <= '9'
                        && c4 == '-'
                        && c5 >= '0' && c5 <= '9'
                        && c6 >= '0' && c6 <= '9'
                        && c7 == '-'
                        && c8 >= '0' && c8 <= '9'
                        && c9 >= '0' && c9 <= '9'
                ) {
                    year = (c0 - '0') * 1000 + (c1 - '0') * 100 + (c2 - '0') * 10 + (c3 - '0');
                    month = (c5 - '0') * 10 + (c6 - '0');
                    dayOfMonth = (c8 - '0') * 10 + (c9 - '0');
                } else {
                    return super.parse(text);
                }

                int dom = 31;
                switch (month) {
                    case 2:
                        boolean isLeapYear = ((year & 3) == 0) && ((year % 100) != 0 || (year % 400) == 0);
                        dom = isLeapYear ? 29 : 28;
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        dom = 30;
                        break;
                }

                if (year >= -999999999 && year <= 999999999
                        && month >= 1 && month <= 12
                        && dayOfMonth >= 1 && dayOfMonth <= dom
                ) {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month - 1);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    if (format10) {
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);

                        return calendar.getTime();
                    }
                } else {
                    return super.parse(text);
                }

                char c10 = text.charAt(10);
                char c11 = text.charAt(11);
                char c12 = text.charAt(12);
                char c13 = text.charAt(13);
                char c14 = text.charAt(14);
                char c15 = text.charAt(15);
                char c16 = text.charAt(16);
                char c17 = text.charAt(17);
                char c18 = text.charAt(18);

                int hour, minute, second;
                if (c10 == ' '
                        && c11 >= '0' && c11 <= '9'
                        && c12 >= '0' && c12 <= '9'
                        && c13 == ':'
                        && c14 >= '0' && c14 <= '9'
                        && c15 >= '0' && c15 <= '9'
                        && c16 == ':'
                        && c17 >= '0' && c17 <= '9'
                        && c18 >= '0' && c18 <= '9'
                ) {
                    hour = (c11 - '0') * 10 + (c12 - '0');
                    minute = (c14 - '0') * 10 + (c15 - '0');
                    second = (c17 - '0') * 10 + (c18 - '0');

                    if (hour >= 0 && hour <= 23
                            && minute >= 0 && minute <= 59
                            && second >= 0 && second <= 59
                    ) {
                        calendar.set(Calendar.HOUR_OF_DAY, hour);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, second);
                        calendar.set(Calendar.MILLISECOND, 0);

                        return calendar.getTime();
                    }
                } else {
                    return super.parse(text);
                }
            }
        }
        return super.parse(text);
    }
}
