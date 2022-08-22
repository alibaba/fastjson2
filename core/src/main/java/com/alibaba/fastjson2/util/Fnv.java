package com.alibaba.fastjson2.util;

// fnv1a 64
public class Fnv {
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

            if (ch == '-' || ch == '_') {
                scoreCount++;
            }
        }

        if (ascii && (name.length() - scoreCount) <= 8) {
            for (int i = 0, j = 0; i < name.length(); i++) {
                char ch = name.charAt(i);
                if (ch == '-' || ch == '_') {
                    continue;
                }
                if (ch >= 'A' && ch <= 'Z') {
                    ch = (char) (ch + 32);
                }

                if (j == 0) {
                    nameValue = (byte) ch;
                } else {
                    nameValue <<= 8;
                    nameValue += ch;
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

            if (ch == '-' || ch == '_') {
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
                for (int i = 0; i < name.length(); i++) {
                    char ch = name.charAt(i);
                    if (i == 0) {
                        nameValue = (byte) ch;
                    } else {
                        nameValue <<= 8;
                        nameValue += ch;
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

    public static long hashCode64(byte... name) {
        if (name.length > 0 && name.length <= 8 && name[0] != 0) {
            long nameValue = 0;
            switch (name.length) {
                case 1:
                    nameValue = name[0];
                    break;
                case 2:
                    nameValue
                            = ((name[0]) << 8)
                            + (name[1] & 0xFF);
                    break;
                case 3:
                    nameValue
                            = ((name[0]) << 16)
                            + ((name[1] & 0xFF) << 8)
                            + (name[2] & 0xFF);
                    break;
                case 4:
                    nameValue
                            = (name[0] << 24)
                            + ((name[1] & 0xFF) << 16)
                            + ((name[2] & 0xFF) << 8)
                            + (name[3] & 0xFF);
                    break;
                case 5:
                    nameValue
                            = (((long) name[0]) << 32)
                            + ((name[1] & 0xFFL) << 24)
                            + ((name[2] & 0xFFL) << 16)
                            + ((name[3] & 0xFFL) << 8)
                            + (name[4] & 0xFFL);
                    break;
                case 6:
                    nameValue
                            = (((long) name[0]) << 40)
                            + ((name[1] & 0xFFL) << 32)
                            + ((name[2] & 0xFFL) << 24)
                            + ((name[3] & 0xFFL) << 16)
                            + ((name[4] & 0xFFL) << 8)
                            + (name[5] & 0xFFL);
                    break;
                case 7:
                    nameValue
                            = (((long) name[0]) << 48)
                            + ((name[1] & 0xFFL) << 40)
                            + ((name[2] & 0xFFL) << 32)
                            + ((name[3] & 0xFFL) << 24)
                            + ((name[4] & 0xFFL) << 16)
                            + ((name[5] & 0xFFL) << 8)
                            + (name[6] & 0xFFL);
                    break;
                case 8:
                    nameValue
                            = (((long) name[0]) << 56)
                            + ((name[1] & 0xFFL) << 48)
                            + ((name[2] & 0xFFL) << 40)
                            + ((name[3] & 0xFFL) << 32)
                            + ((name[4] & 0xFFL) << 24)
                            + ((name[5] & 0xFFL) << 16)
                            + ((name[6] & 0xFFL) << 8)
                            + (name[7] & 0xFFL);
                    break;
                default:
                    break;
            }

            if (nameValue != 0) {
                return nameValue;
            }
        }

        long hashCode = MAGIC_HASH_CODE;
        for (int i = 0; i < name.length; ++i) {
            char ch = (char) name[i];
            hashCode ^= ch;
            hashCode *= MAGIC_PRIME;
        }
        return hashCode;
    }
}
