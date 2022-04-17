package com.alibaba.fastjson2.util;

// fnv1a 64
public class Fnv {
    public static final long MAGIC_HASH_CODE = 0xcbf29ce484222325L;
    public static final long MAGIC_PRIME = 0x100000001b3L;

    public static long hashCode64LCase(String name) {
        long hashCode = MAGIC_HASH_CODE;
        for (int i = 0; i < name.length(); ++i) {
            char ch = name.charAt(i);
            if (ch == '_') {
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
        long hashCode = MAGIC_HASH_CODE;
        for (int i = 0; i < name.length(); ++i) {
            char ch = name.charAt(i);
            hashCode ^= ch;
            hashCode *= MAGIC_PRIME;
        }
        return hashCode;
    }
}
