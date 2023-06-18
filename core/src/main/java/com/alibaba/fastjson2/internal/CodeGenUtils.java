package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.util.IOUtils;

public class CodeGenUtils {
    public static String fieldReader(int i) {
        switch (i) {
            case 0:
                return "fieldReader0";
            case 1:
                return "fieldReader1";
            case 2:
                return "fieldReader2";
            case 3:
                return "fieldReader3";
            case 4:
                return "fieldReader4";
            case 5:
                return "fieldReader5";
            case 6:
                return "fieldReader6";
            case 7:
                return "fieldReader7";
            case 8:
                return "fieldReader8";
            case 9:
                return "fieldReader9";
            case 10:
                return "fieldReader10";
            case 11:
                return "fieldReader11";
            case 12:
                return "fieldReader12";
            case 13:
                return "fieldReader13";
            case 14:
                return "fieldReader14";
            case 15:
                return "fieldReader15";
            default:
                String base = "fieldReader";
                final int baseSize = base.length();
                int size = IOUtils.stringSize(i);
                char[] chars = new char[baseSize + size];
                base.getChars(0, baseSize, chars, 0);
                IOUtils.writeInt32(chars, baseSize, i);
                return new String(chars);
        }
    }
}
