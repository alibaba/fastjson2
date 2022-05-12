package com.alibaba.fastjson2.annotation;

/**
 * @since 1.2.15
 */
public enum NamingStrategy {
    CamelCase,
    PascalCase,
    SnakeCase,
    UpperCase,
    KebabCase;

    public static String snakeToCamel(String name) {
        if (name == null || name.indexOf('_') == -1) {
            return name;
        }

        int underscoreCount = 0;
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == '_') {
                underscoreCount++;
            }
        }

        char[] chars = new char[name.length() - underscoreCount];
        for (int i = 0, j = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == '_') {
                continue;
            }
            if (i > 0 && name.charAt(i - 1) == '_') {
                if (ch >= 'a' && ch <= 'z') {
                    ch -= 32;
                }
            }
            chars[j++] = ch;
        }

        return new String(chars);
    }
}
