package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.util.IOUtils;

/**
 * Utility class for generating code-related names and identifiers.
 * Provides methods to generate standardized field reader names for bytecode generation.
 */
public class CodeGenUtils {
    /**
     * Generates a standardized field reader name for the given index.
     * Returns cached names for indices 0-15, and dynamically generates names for higher indices.
     *
     * @param i the field reader index
     * @return the field reader name in the format "fieldReaderN" where N is the index
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * String name0 = CodeGenUtils.fieldReader(0);  // returns "fieldReader0"
     * String name5 = CodeGenUtils.fieldReader(5);  // returns "fieldReader5"
     * String name20 = CodeGenUtils.fieldReader(20); // returns "fieldReader20"
     * }</pre>
     */
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
