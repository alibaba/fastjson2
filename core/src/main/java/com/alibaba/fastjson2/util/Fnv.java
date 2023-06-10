package com.alibaba.fastjson2.util;

// fnv1a 64
public final class Fnv {
    public static final long MAGIC_HASH_CODE = 0xcbf29ce484222325L;
    public static final long MAGIC_PRIME = 0x100000001b3L;

    public static long hashCode64LCase(String name) {
        boolean ascii = true;
        long nameValue = 0;

        int scoreCount = 0;
        for (int i = 0; i < name.length(); ++i) {
            char ch = name.charAt(i);
            if (ch > 0xFF || (i == 0 && ch == 0)) {
                ascii = false;
                break;
            }

            if (ch == '-' || ch == '_' || ch == ' ') {
                scoreCount++;
            }
        }

        if (ascii && (name.length() - scoreCount) <= 8) {
            for (int i = name.length() - 1, j = 0; i >= 0; --i) {
                char ch = name.charAt(i);
                if (ch == '-' || ch == '_' || ch == ' ') {
                    continue;
                }
                if (ch >= 'A' && ch <= 'Z') {
                    ch = (char) (ch + 32);
                }

                if (j == 0) {
                    nameValue = (byte) ch;
                } else {
                    nameValue = (nameValue << 8) + ch;
                }
                j++;
            }

            if (nameValue != 0) {
                return nameValue;
            }
        }

        long hashCode = MAGIC_HASH_CODE;
        for (int i = 0; i < name.length(); ++i) {
            char ch = name.charAt(i);

            if (ch == '-' || ch == '_' || ch == ' ') {
                continue;
            }
            if (ch >= 'A' && ch <= 'Z') {
                ch = (char) (ch + 32);
            }

            hashCode ^= ch;
            hashCode *= MAGIC_PRIME;
        }
        return hashCode;
    }

    public static long hashCode64(String name) {
        if (name.length() <= 8) {
            boolean ascii = true;
            long nameValue = 0;

            for (int i = 0; i < name.length(); ++i) {
                char ch = name.charAt(i);
                if (ch > 0xFF || (i == 0 && ch == 0)) {
                    ascii = false;
                    break;
                }
            }

            if (ascii) {
                for (int i = name.length() - 1; i >= 0; --i) {
                    char ch = name.charAt(i);
                    if (i == name.length() - 1) {
                        nameValue = (byte) ch;
                    } else {
                        nameValue = (nameValue << 8) + ch;
                    }
                }

                if (nameValue != 0) {
                    return nameValue;
                }
            }
        }

        long hashCode = MAGIC_HASH_CODE;
        for (int i = 0; i < name.length(); ++i) {
            char ch = name.charAt(i);
            hashCode ^= ch;
            hashCode *= MAGIC_PRIME;
        }
        return hashCode;
    }
}
