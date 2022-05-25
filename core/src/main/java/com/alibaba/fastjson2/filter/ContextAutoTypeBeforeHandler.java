package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.fastjson2.util.TypeUtils.loadClass;

public class ContextAutoTypeBeforeHandler
        implements JSONReader.AutoTypeBeforeHandler {
    final long[] acceptHashCodes;
    final Map<String, Class> classCache = new ConcurrentHashMap<>(16, 0.75f, 1);

    public ContextAutoTypeBeforeHandler(String[] accessNames) {
        long[] array = new long[accessNames.length];

        int index = 0;
        for (int i = 0; i < accessNames.length; i++) {
            String name = accessNames[i];

            if (name == null || name.isEmpty()) {
                continue;
            }

            long hashCode = Fnv.hashCode64(name);
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
        long hash = Fnv.MAGIC_HASH_CODE;
        for (int i = 0, typeNameLength = typeName.length(); i < typeNameLength; ++i) {
            char ch = typeName.charAt(i);
            if (ch == '$') {
                ch = '.';
            }
            hash ^= ch;
            hash *= Fnv.MAGIC_PRIME;

            if (Arrays.binarySearch(acceptHashCodes, hash) >= 0) {
                Class clazz = classCache.get(typeName);

                if (clazz == null) {
                    clazz = loadClass(typeName);
                    Class origin = classCache.putIfAbsent(typeName, clazz);
                    if (origin != null) {
                        clazz = origin;
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
