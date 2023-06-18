package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.util.IOUtils;

public class CodeGenUtils {
    static final String[] fieldItemObjectReader = new String[1024];

    public static Class getSupperClass(int fieldReaders) {
        Class objectReaderSuper;
        switch (fieldReaders) {
            case 1:
                objectReaderSuper = ObjectReader1.class;
                break;
            case 2:
                objectReaderSuper = ObjectReader2.class;
                break;
            case 3:
                objectReaderSuper = ObjectReader3.class;
                break;
            case 4:
                objectReaderSuper = ObjectReader4.class;
                break;
            case 5:
                objectReaderSuper = ObjectReader5.class;
                break;
            case 6:
                objectReaderSuper = ObjectReader6.class;
                break;
            case 7:
                objectReaderSuper = ObjectReader7.class;
                break;
            case 8:
                objectReaderSuper = ObjectReader8.class;
                break;
            case 9:
                objectReaderSuper = ObjectReader9.class;
                break;
            case 10:
                objectReaderSuper = ObjectReader10.class;
                break;
            case 11:
                objectReaderSuper = ObjectReader11.class;
                break;
            case 12:
                objectReaderSuper = ObjectReader11.class;
                break;
            default:
                objectReaderSuper = ObjectReaderAdapter.class;
                break;
        }
        return objectReaderSuper;
    }

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

    public static String fieldObjectReader(int i) {
        switch (i) {
            case 0:
                return "objectReader0";
            case 1:
                return "objectReader1";
            case 2:
                return "objectReader2";
            case 3:
                return "objectReader3";
            case 4:
                return "objectReader4";
            case 5:
                return "objectReader5";
            case 6:
                return "objectReader6";
            case 7:
                return "objectReader7";
            case 8:
                return "objectReader8";
            case 9:
                return "objectReader9";
            case 10:
                return "objectReader10";
            case 11:
                return "objectReader11";
            case 12:
                return "objectReader12";
            case 13:
                return "objectReader13";
            case 14:
                return "objectReader14";
            case 15:
                return "objectReader15";
            default:
                String base = "objectReader";
                final int baseSize = base.length();
                int size = IOUtils.stringSize(i);
                char[] chars = new char[baseSize + size];
                base.getChars(0, baseSize, chars, 0);
                IOUtils.writeInt32(chars, baseSize, i);
                return new String(chars);
        }
    }

    public static String fieldItemObjectReader(int i) {
        String fieldName = fieldItemObjectReader[i];

        if (fieldName != null) {
            return fieldName;
        }

        String base = "itemReader";
        final int baseSize = base.length();
        int size = IOUtils.stringSize(i);
        char[] chars = new char[baseSize + size];
        base.getChars(0, baseSize, chars, 0);
        IOUtils.writeInt32(chars, baseSize, i);
        return new String(chars);
    }
}
