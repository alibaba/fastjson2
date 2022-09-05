package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSONReader;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.fastjson2.util.Fnv.MAGIC_HASH_CODE;
import static com.alibaba.fastjson2.util.Fnv.MAGIC_PRIME;
import static com.alibaba.fastjson2.util.TypeUtils.loadClass;

public class ContextAutoTypeBeforeHandler
        implements JSONReader.AutoTypeBeforeHandler {
    final long[] acceptHashCodes;
    final Map<String, Class> classCache = new ConcurrentHashMap<>(16, 0.75f, 1);

    public ContextAutoTypeBeforeHandler(String[] acceptNames) {
        long[] array = new long[acceptNames.length];

        int index = 0;
        for (int i = 0; i < acceptNames.length; i++) {
            String name = acceptNames[i];

            if (name == null || name.isEmpty()) {
                continue;
            }

            long hashCode = MAGIC_HASH_CODE;
            for (int j = 0; j < name.length(); ++j) {
                char ch = name.charAt(j);
                hashCode ^= ch;
                hashCode *= MAGIC_PRIME;
            }

            array[index++] = hashCode;
        }

        if (index != array.length) {
            array = Arrays.copyOf(array, index);
        }
        Arrays.sort(array);
        this.acceptHashCodes = array;
    }

    @Override
    public Class<?> apply(String typeName, Class<?> expectClass, long features) {
        long hash = MAGIC_HASH_CODE;
        for (int i = 0, typeNameLength = typeName.length(); i < typeNameLength; ++i) {
            char ch = typeName.charAt(i);
            if (ch == '$') {
                ch = '.';
            }
            hash ^= ch;
            hash *= MAGIC_PRIME;

            if (Arrays.binarySearch(acceptHashCodes, hash) >= 0) {
                Class clazz = classCache.get(typeName);

                if (clazz == null) {
                    clazz = loadClass(typeName);
                    if (clazz != null) {
                        Class origin = classCache.putIfAbsent(typeName, clazz);
                        if (origin != null) {
                            clazz = origin;
                        }
                    }
                }

                if (clazz != null) {
                    return clazz;
                }
            }
        }

        return null;
    }
}
