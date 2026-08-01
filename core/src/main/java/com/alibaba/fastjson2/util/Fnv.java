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

                nameValue = (nameValue << 8) | ch;
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

    public static long hashCode64(String... names) {
        if (names.length == 1) {
            return hashCode64(names[0]);
        }

        long hashCode = MAGIC_HASH_CODE;

        for (String name : names) {
            long h = hashCode64(name);
            hashCode ^= h;
            hashCode *= MAGIC_PRIME;
        }

        return hashCode;
    }

    public static long hashCode64(byte[] bytes, int offset, int len, boolean ascii) {
        if (!ascii) {
            return hashCode64UTF8(bytes, offset, len);
        }
        if (len > 0 && len <= 8) {
            long nameValue = IOUtils.getLongLE(bytes, offset) & (0xFFFFFFFFFFFFFFFFL >>> ((8 - len) << 3));
            if (nameValue != 0) {
                return nameValue;
            }
        }

        long hashCode = MAGIC_HASH_CODE;
        for (int i = 0; i < len; ++i) {
            byte ch = bytes[offset + i];
            hashCode ^= ch;
            hashCode *= MAGIC_PRIME;
        }
        return hashCode;
    }

    private static long hashCode64UTF8(byte[] bytes, int offset, int len) {
        char[] chars = new char[len];
        int char_len = IOUtils.decodeUTF8(bytes, offset, len, chars);
        return hashCode64(chars, 0, char_len);
    }

    public static long hashCode64(char[] chars, int offset, int len) {
        if (len <= 8) {
            boolean ascii = true;
            long nameValue = 0;

            for (int i = 0; i < len; ++i) {
                char ch = chars[offset + i];
                if (ch > 0xFF || (i == 0 && ch == 0)) {
                    ascii = false;
                    break;
                }
            }

            if (ascii) {
                for (int i = len - 1; i >= 0; --i) {
                    char ch = chars[offset + i];
                    nameValue = (nameValue << 8) | ch;
                }

                if (nameValue != 0) {
                    return nameValue;
                }
            }
        }

        long hashCode = MAGIC_HASH_CODE;
        for (int i = 0; i < len; ++i) {
            char ch = chars[offset + i];
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
                    nameValue = (nameValue << 8) | ch;
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
